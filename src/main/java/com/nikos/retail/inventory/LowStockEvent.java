package com.nikos.retail.inventory;

public record LowStockEvent(Long variantId, String sku, Long locationId, String locationName, int availableQuantity) {
}