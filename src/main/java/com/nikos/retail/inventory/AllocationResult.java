package com.nikos.retail.inventory;

public record AllocationResult(int reservedQuantity, int backorderedQuantity, boolean rejected) {
}