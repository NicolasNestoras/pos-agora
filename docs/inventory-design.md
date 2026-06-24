# Inventory feature

## Problem & Goals
Track stock levels, for each ProductVariant, saving the history for each adjustment, and prevent overselling during checkout. The structure of the inventory feature will be more involved. However, I will build a basic version to test now, and make the necessary design changes after.

## Data Model
We have two options.
    1. The current design-which is to have stockQuantity directly in ProductVariant, and to reduce it during Sales and Orders. Although this works, we miss a key part of the inventory management-audit trail.
    2. An InventoryMovement table: id, referenceId(optional: Sale/Order),productVariantId, quantityChange, inventoryMovementType, createdAt.
    Current stock = calculated from movements.

I will implement the second model. In addition, I will adjust the logic so that Orders don't reduce stock, but will instead apply a Stock Reservation.
In this way we can calculate available stock through:
    available_stock = stock_quantity-reserved_quantity

## Entities
 
```
InventoryMovement
  id
  referenceId
  quantityChange
  productVariantId
  inventoryMovementType
  note
  createdAt

```

## Additional modifications of files.
I will be modifying the SaleService and OrderService, to match the current StockQuantity changes.

