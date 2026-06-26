# Retail POS and Ecommerce

A backend system for a unified retail platform — designed to power an in-person POS (point of sale) system and a future e-commerce storefront from a single, shared domain model.

Built with Spring Boot, PostgreSQL, and Flyway-managed schema migrations.

## Why this project exists

Most small retail businesses end up running two disconnected systems: a POS for the physical shop, and a separate e-commerce platform online — with inventory, pricing, and customer data duplicated (and drifting out of sync) between them. Agora Retail is a backend designed to serve both from one source of truth: one product catalog, one inventory ledger, one customer base, with separate checkout flows (`Sale` for in-person, `Order` for online) built on top.

This project was built as a hands-on way to learn backend engineering properly — from raw JDBC up to a layered Spring Boot REST API — while making deliberate architectural decisions along the way rather than copying a template.

## Tech stack

- **Java 21**
- **Spring Boot 3.3.5** — Web, Data JPA, Validation, Security
- **PostgreSQL** — primary datastore
- **Flyway 10.20.1** — versioned, repeatable schema migrations
- **Maven** — dependency management and build lifecycle
- **JUnit 5 + Mockito** — unit testing

## Architecture

The codebase is organized **by feature, not by layer** — each business domain owns its entities, repositories, services, controllers, and DTOs in one package, rather than scattering related code across global `model/`, `service/`, `controller/` folders. This keeps related logic together and scales cleanly as features are added.

```
src/main/java/com/nikos/retail/
├── RetailApplication.java
├── common/
│   ├── exception/      → ResourceNotFoundException, GlobalExceptionHandler
│   └── dto/            → ErrorResponse (shared API error shape)
├── product/             → Product catalog
├── productvariant/      → Variants, price lists, wholesale/retail pricing
├── customer/            → Customer accounts (retail & wholesale)
├── cart/                → Shared cart used by both POS and e-commerce
├── sales/                → POS checkout (instant, in-person transactions)
├── order/                → E-commerce checkout (multi-stage lifecycle)
├── payment/              → Payments, linked to either a Sale or an Order
```

### Layering within each feature

```
Controller   → translates HTTP requests/responses, no business logic
Service      → business rules, validation, orchestration, @Transactional boundaries
Repository   → Spring Data JPA interfaces, no manual SQL
Entity       → JPA-mapped database row, never exposed directly over the API
DTO          → Request/Response shapes that decouple the API contract from the schema
```

Controllers and the outside world never see entities directly — every endpoint accepts a `*Request` DTO and returns a `*Response` DTO. This avoids leaking internal fields, prevents infinite serialization recursion across bidirectional JPA relationships, and means the database schema can evolve independently of the API contract.

## Key design decisions

**Cart is shared between POS and e-commerce.**
A cashier holds a cart while ringing up a customer; an online shopper holds a cart while browsing. Both flows convert a `Cart` into a permanent record at checkout — a `Sale` for POS, an `Order` for e-commerce — rather than forcing both into one entity with two different lifecycles bolted together.

**Sale and Order are separate entities, not one polymorphic "Order."**
A POS sale completes instantly with no shipping. An e-commerce order moves through `PENDING → PAID → SHIPPED → DELIVERED` over days. Modeling them separately keeps each one's invariants simple and explicit, at the cost of some duplicated shape between `SaleItem` and `OrderItem` — a deliberate tradeoff in favor of clarity over DRY-ness.

**Prices are snapshotted at the line-item level, not looked up live.**
`CartItem`, `SaleItem`, and `OrderItem` all store `unitPrice` at the moment the item was added — not a live reference to the product variant's current price. This guarantees a customer's total never silently changes if the store updates a price while their cart is open, mirroring how real POS/e-commerce systems behave.

**Wholesale pricing uses a price-list model, not a flat discount.**
Rather than hardcoding "wholesale = 80% of retail," `PriceList` and `VariantPrice` allow per-variant price overrides per customer tier. `CartService` resolves the correct price based on the customer's `CustomerType` at the time an item is added. This mirrors how Shopify B2B and similar real systems handle tiered pricing.


**Payment uses two nullable foreign keys instead of duplicated payment logic.**
A `Payment` can reference either a `Sale` or an `Order`, enforced as "exactly one of the two" both in the service layer (clear error messages) and via a PostgreSQL `CHECK` constraint (defense in depth, in case the service layer is ever bypassed).

**Schema is managed exclusively through Flyway, not Hibernate auto-DDL.**
Every schema change is a versioned, checked-in SQL file under `src/main/resources/db/migration/`. Hibernate is configured with `ddl-auto=validate` — it verifies entity mappings match the real schema at startup, but never creates or alters tables itself.

## Database schema

| Migration | Adds |
|---|---|
| V1 | `products` |
| V2 | `product_variants` |
| V3 | `customers` (with retail/wholesale type) |
| V4 | `carts`, `cart_items` |
| V5 | `sales`, `sale_items` |
| V6 | `orders`, `order_items` |
| V7 | `price_lists`, `variant_prices` (+ seeds RETAIL/WHOLESALE) |
| V8 | `payments` (+ exactly-one-of CHECK constraint) |

## API overview

| Resource | Endpoints |
|---|---|
| Products | `GET/POST /api/products`, `GET /api/products/{id}` |
| Variants | `GET/POST /api/products/{productId}/variants`, `POST /api/products/{productId}/variants/{variantId}/prices` |
| Customers | `GET/POST /api/customers`, `GET /api/customers/{id}` |
| Carts | `POST /api/carts`, `GET /api/carts/{id}`, `POST /api/carts/{id}/items`, `POST /api/carts/{id}/hold`, `POST /api/carts/{id}/resume`, `GET /api/carts/held` |
| Sales | `POST /api/sales/checkout?cartId=`, `GET /api/sales/{id}` |
| Orders | `POST /api/orders`, `GET /api/orders/{id}`, `GET /api/orders/customer/{customerId}`, `PATCH /api/orders/{id}/status` |
| Payments | `POST /api/payments`, `GET /api/payments/{id}` |
| Locations | `GET/POST /api/locations`, `GET /api/locations/{id}` |
| Inventory | `GET api/inventory/stock/location/{locationId}`, `GET api/inventory/stock/variant/{variantId}`, `POST api/inventory/stock/stock/adjust`|
| Stock Transfers | `POST api/inventory/transfers` |

All errors return a consistent shape via a global exception handler:
```json
{
  "timestamp": "2026-06-20T10:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Product not found with id: 99",
  "path": "/api/products/99"
}
```

## Running locally

```bash
# create the database
createdb Retail

# set required environment variables
export DB_PASSWORD=your_postgres_password

# run the app - Flyway migrates the schema automatically on startup
mvn spring-boot:run
```

Configuration lives in `src/main/resources/application.properties` and is externalized via environment variables (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`) — no credentials are committed to the repository.

## Testing

Unit tests cover the business rules most at risk during refactors — invalid state transitions, stock guards, and cross-field validation — using JUnit 5 and Mockito to isolate each service from the database.

```bash
mvn test
```

## Roadmap
- [x] `inventory/` - multi-location stock (store/warehouse), reservations, backorders, manual stock adjustments, inter-location transfers
+ [x] Concurrency-safe stock allocation via pessimistic row locking
- [ ] `inventory/` - manual review workflow for backorder fulfillment on short shipments
- [ ] Improvement in performance.(Fix the current N+1 Query problem)
- [ ] `shipment/` — tracking for e-commerce order fulfillment
- [ ] `user/` + `security/` — JWT authentication, role-based access (cashier vs admin vs customer)
- [ ] Refund flow for both `Sale` and `Order`
- [ ] Payment method specified for retail(only cash/card).
- [ ] Addition of stripe for safe payment handling by card.
- [ ] Reporting endpoints (daily sales totals, low-stock alerts)
- [ ] OpenAPI/Swagger documentation
- [ ] Dockerized local setup (Postgres + app via docker-compose)
- [ ] Data warehouse for analytics in Inventory, Sales, Orders(ERP), and Customer behaviour (CRM)


## What this project demonstrates

- Layered architecture with clear separation of concerns (controller / service / repository / entity / DTO)
- Deliberate data modeling for real-world constraints (anonymous vs known customers, price tiers, audit trails)
- Transactional integrity across multi-step operations (`@Transactional` checkout flows with rollback safety)
- Defense-in-depth validation (application-level checks backed by database constraints)
- Versioned, reproducible database schema via Flyway
- Unit-tested business logic, isolated from infrastructure via mocking