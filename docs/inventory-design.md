# Inventory Design

## 1. Problem & Goal

Track stock across multiple physical locations (store, warehouse), prevent overselling,
and support two fundamentally different sales motions on top of the same catalog:

- **Retail** customers can only buy what's actually available right now.
- **Wholesale** customers can order ahead of stock existing — including stock that
  hasn't been manufactured or imported yet — with delivery weeks out.

This feature must integrate with the existing `Sale` (instant, in-person) and `Order`
(multi-stage) checkout flows without changing their entities, and must respect the
existing `CustomerType` (retail/wholesale) distinction already used for pricing.

## 2. Core Concepts

| Term | Meaning |
|---|---|
| **On-hand quantity** | Physically present at a specific location right now. |
| **Reserved quantity** | Committed to a pending order, at a specific location, not yet shipped/fulfilled. |
| **Available quantity** | `on_hand - reserved`. What can actually be sold immediately. Never stored — always computed. |
| **Backorder** | A quantity promised to a wholesale customer that exceeds currently available stock, anywhere. Not tied to a location until fulfilled. |
| **Location** | A real, persisted entity — not an enum — representing a place stock physically exists (e.g. Store, Warehouse). Modeled as a table to allow future locations (second store, second warehouse) without a migration. |

## 3. Channel & Customer Rules

This is the central decision table. Stock behavior is driven by **checkout type +
customer type**, not by "POS vs ecommerce" — POS is capable of producing both
checkout types.

| Checkout type | Customer type | Stock rule | Draws from |
|---|---|---|---|
| `Sale` (instant, in-person) | — | Strict. Reject if `requestedQty > availableQuantity`. No backorder possible — customer leaves with the product. | **The location the sale physically occurs at** (for now: only one Store). |
| `Order` | Retail | Strict. Reject if `requestedQty > availableQuantity`. | **Warehouse**, always — retail is online-only for now. |
| `Order` | Wholesale | Allow partial backorder. Reserve what's available, backorder the remainder. | **Warehouse**, always — even when the order is placed in-store at the POS. |

Key point: a wholesale customer placing an order *at the physical store* still reserves
against **warehouse** stock, never store stock. Store stock is reserved for `Sale`
walk-outs only. This is a deliberate business rule, a choice I made based on the requirements of this business.

**Why "the location the sale occurs at" instead of a hardcoded Store:** there's
currently one store, but the business may open a second store, or have an existing
store start selling retail too. Rather than hardcoding `Sale → Store`, `Sale` should
carry a `locationId` (the location the POS session/terminal is assigned to), and the
allocation rule is really "draw from wherever the handoff physically happens." Right now, since there is only one store, this behaves identically to a hardcoded rule — but it means
opening a second store or adding retail at an existing store needs no changes to
the logic, only a new `Location` row and a POS terminal pointed at it. This is the
practical payoff of having modeled `Location` as a real table instead of an enum
back in section 2.

## 4. Entities

```
Location
  id
  name
  type            (STORE / WAREHOUSE)         -- informational, not the source of routing logic

VariantStock
  id
  variantId
  locationId
  onHandQuantity
  reservedQuantity
  UNIQUE (variantId, locationId)

StockMovement
  id
  variantId
  locationId
  quantityChange      -- positive or negative (non-zero)
  reason              (RESTOCK / SALE / ORDER_FULFILLED / RETURN / ADJUSTMENT / TRANSFER_IN / TRANSFER_OUT)
  referenceId         -- e.g. saleId, orderId, transferId
  createdAt

Reservation
  id
  variantId
  locationId
  orderId
  quantity
  status              (ACTIVE / COMMITTED / RELEASED)
  expiresAt
  createdAt

Backorder
  id
  variantId
  orderId
  quantity
  status              (PENDING / FULFILLED)
  linkedIncomingStockId   -- nullable; a backorder can exist before a confirmed shipment

IncomingStock
  id
  variantId
  locationId
  expectedQuantity
  expectedDate
  status              (EXPECTED / RECEIVED)

StockTransfer
  id
  variantId
  fromLocationId
  toLocationId
  quantity
  status              (PENDING / COMPLETED)
  createdAt
```

`ProductVariant` itself gains **no quantity fields** — `stockQuantity` is removed from
it entirely. "Total stock" for a variant, if ever needed (e.g. a product page), is
computed as `SUM(onHandQuantity) FROM VariantStock WHERE variantId = ?` across all
locations — same as `availableQuantity`: derived, don't stored.

## 5. Allocation Logic

A new service, `StockAllocationService` (in `inventory/`), owns the decision of how
much of a requested quantity gets reserved vs. backordered:

```java
record AllocationResult(int reservedQty, int backorderedQty, boolean rejected)

AllocationResult allocate(UUID variantId, UUID locationId, int requestedQty, CustomerType customerType)
```

- **Retail** (or any `Sale`): if `requestedQty > available at locationId` → `rejected = true`. No reservation, no backorder.
- **Wholesale `Order`**: `reservedQty = min(requestedQty, available at warehouse)`, `backorderedQty = requestedQty - reservedQty`. Never rejected on stock grounds alone.

Called once per line item, inside the same `@Transactional` boundary as the
`Sale`/`Order` creation — consistent with existing checkout atomicity. Each call that
reserves stock writes a `Reservation` row and increments `VariantStock.reservedQuantity`
in the same transaction; each call that backorders writes a `Backorder` row only
(no stock numbers move, since nothing exists yet to reserve).

## 6. Stock Transfers

Moving stock from warehouse to store (e.g. restocking the shop floor) is a **manual
action by staff** — no automatic triggering on `IncomingStock` arrival or on pending
orders. Implemented as two `StockMovement` rows under one transaction:

- `TRANSFER_OUT` at `fromLocationId` (decrement)
- `TRANSFER_IN` at `toLocationId` (increment)

Both wrapped in a single `StockTransfer` record for traceability — same double-entry
instinct already used for `Payment`'s exactly-one-of constraint, applied here to
movement instead of to a foreign key.

## 6.1 Backorder Fulfillment — Manual Review

When `IncomingStock` arrives, it does **not** automatically fulfill outstanding
`Backorder`s if the arriving quantity is less than total backordered demand for that
variant. Auto-fulfilling in arrival order (e.g. oldest backorder first) is an easy
trap — it silently becomes a FIFO policy without anyone deciding that's what should
happen, and FIFO may not be what the business wants. In this case, the business wants to manually check and pick the order in which the clients get the items. 

Instead, when received quantity < total pending backorder demand for a variant:

1. No `Reservation`s are auto-created from the new stock.
2. The system surfaces a **shortfall view** — e.g. `GET /api/inventory/backorders/{variantId}/pending` — listing all `PENDING` backorders for that variant (customer, quantity, order date) alongside the quantity that just arrived.
3. A staff member manually decides the split and submits it — e.g. `POST /api/inventory/backorders/{id}/fulfill { quantity }` once per backorder they choose to (partially or fully) fulfill.
4. Each fulfillment call creates a `Reservation` for the allocated quantity and reduces the `Backorder.quantity` accordingly (or marks it `FULFILLED` if fully covered).

If received quantity ≥ total pending backorder demand, there's no actual decision to
make — all pending backorders can simply be fulfilled in full automatically. The
manual step only exists for the genuinely ambiguous case: a shortfall.

## 7. Lifecycle Example (Wholesale Order, Placed In-Store)

```
1. Wholesale customer orders 100 units at the POS.
2. StockAllocationService checks warehouse availability: 60 available.
3. Reservation created: qty=60, status=ACTIVE, locationId=warehouse.
   VariantStock.reservedQuantity (warehouse) += 60.
4. Backorder created: qty=40, status=PENDING, linkedIncomingStockId=null (no shipment confirmed yet).
5. [Weeks later] IncomingStock (warehouse, qty=50) arrives — short of the 40 backordered (in this case enough to cover it, so no shortfall — auto-fulfilled).
   StockMovement RESTOCK +50 at warehouse. VariantStock.onHandQuantity += 50.
6. Since 50 received ≥ 40 pending backorder, it's auto-fulfilled in full:
   Reservation created for the 40. Backorder.status = FULFILLED.
   (Had received quantity been less than 40, this step would instead surface the shortfall view for manual staff review — see section 6.1.)
7. Staff manually transfers some warehouse stock to the store for shelf restocking — unrelated to this order, store stock is never touched by it.
8. Order ships → all ACTIVE reservations for this order become COMMITTED.
   StockMovement ORDER_FULFILLED -100 at warehouse. VariantStock.onHandQuantity -= 100, reservedQuantity -= 100.
```

## 8. Testing Plan

- Retail `Order`/`Sale` checkout rejects when `requestedQty > availableQuantity`.
- Wholesale `Order` checkout with insufficient stock produces correct split between `Reservation` and `Backorder`.
- `Sale` always draws from store location; wholesale `Order` always draws from warehouse, even when placed via POS.
- Concurrent checkouts against the same low-stock variant never push `availableQuantity` negative (integration test, good Testcontainers candidate, ties into the existing concurrency roadmap item).
- `StockTransfer` correctly produces both `TRANSFER_OUT` and `TRANSFER_IN` movements, fails atomically if either side fails.
- Backorder fulfillment against arriving `IncomingStock` correctly creates a `Reservation` and flips status to `FULFILLED` when received quantity covers it in full.
- When received quantity is less than total pending backorder demand, no `Reservation`s are auto-created and the shortfall view returns the correct pending backorders for that variant.
- Manual fulfillment endpoint correctly partially or fully fulfills a backorder and updates its remaining quantity/status.

## 9. Open Questions

- **`Sale` needs a `locationId` field.** Currently `Sale` (in `sales/`) has no
  concept of location. Since allocation now depends on "the location the sale
  physically occurs at" (section 3), `Sale` needs this column added — populated from
  whichever `Location` the POS terminal/session is assigned to. With one store today
  it'll always resolve to the same value, but the field should exist now to avoid a
  migration + backfill later when a second location opens.
- **Partial allocation on retail rejection:** if a retail order has multiple line items and only one is short, does the whole checkout fail, or just that line? Leaning toward failing the whole checkout for simplicity and clearer customer expectations.
- **Reservation expiry:** does a `Reservation` need a TTL/expiry job for orders that stall before payment, similar to typical checkout-hold patterns? Not yet addressed — may not be needed if wholesale orders don't have an abandon-prone checkout session the way ecommerce carts do.
- **Consistency discipline:** `VariantStock.reservedQuantity`/`onHandQuantity` are cached counters kept in sync with `Reservation`/`StockMovement` inside transactional boundaries. A periodic reconciliation job recomputing these from the ledger would catch drift in production — not built yet, worth doing before this goes live for real.
- **Manual review UI/permissions:** who is allowed to fulfill a shortfall backorder (section 6.1) — any staff member, or a specific role? Out of scope until the `user/`/`security/` roadmap item exists, but worth a one-line TODO so it isn't forgotten.

## 10. Migrations Needed

- `V10__create_locations.sql`
- `V11__create_variant_stock.sql`   
- `V12__create_stock_movements.sql`
- `V13__create_reservations.sql`
- `V14__create_backorders.sql`
- `V15__create_incoming_stock.sql`
- `V16__create_stock_transfers.sql`
- `V17__drop_stock_quantity_from_product_variants.sql`
- `V18__add_location_to_sales.sql`
