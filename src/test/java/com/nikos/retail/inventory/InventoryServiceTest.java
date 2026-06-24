package com.nikos.retail.inventory;

import com.nikos.retail.productvariant.ProductVariant;
import com.nikos.retail.productvariant.ProductVariantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock private InventoryMovementRepository inventoryMovementRepository;
    @Mock private ProductVariantRepository productVariantRepository;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void recordMovement_shouldThrow_whenResultingStockIsNegative() {
        ProductVariant variant = new ProductVariant();
        variant.setStockQuantity(2);

        when(productVariantRepository.findById(1L)).thenReturn(Optional.of(variant));

        assertThrows(IllegalStateException.class,
            () -> inventoryService.recordMovement(1L, InventoryMovementType.ADJUSTMENT, -5, null, "test"));
    }
}