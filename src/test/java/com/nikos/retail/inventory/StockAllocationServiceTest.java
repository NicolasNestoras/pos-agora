package com.nikos.retail.inventory;

import com.nikos.retail.order.Order;
import com.nikos.retail.productvariant.ProductVariant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockAllocationServiceTest {

    @Mock private VariantStockRepository variantStockRepository;
    @Mock private ReservationRepository reservationRepository;
    @Mock private BackorderRepository backorderRepository;
    @Mock private StockMovementRepository stockMovementRepository;

    @InjectMocks
    private StockAllocationService stockAllocationService;

    private ProductVariant productVariant;
    private Location warehouse;
    private Order order;
    private VariantStock stock;

    @BeforeEach
    void setUp() {
        productVariant = new ProductVariant();
        productVariant.setSku("SKU-1");

        warehouse = new Location();
        warehouse.setName("Main Warehouse");
        warehouse.setType(LocationType.WAREHOUSE);

        order = new Order();

        stock = new VariantStock();
        stock.setProductVariant(productVariant);
        stock.setLocation(warehouse);
    }

    @Test
    void retailWithSufficientStock_reservesFullAmount() {
        stock.setOnHandQuantity(10);
        when(variantStockRepository.findForUpdate(any(), any())).thenReturn(Optional.of(stock));

        AllocationResult result = stockAllocationService.allocate(productVariant, warehouse, 5, false, order);

        assertEquals(5, result.reservedQuantity());
        assertEquals(0, result.backorderedQuantity());
        assertEquals(5, stock.getReservedQuantity());
    }

    @Test
    void retailWithInsufficientStock_throws() {
        stock.setOnHandQuantity(3);
        when(variantStockRepository.findForUpdate(any(), any())).thenReturn(Optional.of(stock));

        assertThrows(IllegalStateException.class, () ->
            stockAllocationService.allocate(productVariant, warehouse, 5, false, order));
    }

    @Test
    void wholesaleWithSufficientStock_reservesFullAmount_noBackorder() {
        stock.setOnHandQuantity(10);
        when(variantStockRepository.findForUpdate(any(), any())).thenReturn(Optional.of(stock));

        AllocationResult result = stockAllocationService.allocate(productVariant, warehouse, 5, true, order);

        assertEquals(5, result.reservedQuantity());
        assertEquals(0, result.backorderedQuantity());
    }

    @Test
    void wholesaleWithInsufficientStock_reservesAvailable_backordersRemainder() {
        stock.setOnHandQuantity(3);
        when(variantStockRepository.findForUpdate(any(), any())).thenReturn(Optional.of(stock));

        AllocationResult result = stockAllocationService.allocate(productVariant, warehouse, 5, true, order);

        assertEquals(3, result.reservedQuantity());
        assertEquals(2, result.backorderedQuantity());
        assertEquals(3, stock.getReservedQuantity());
    }

    @Test
    void recordSale_withSufficientStock_decrementsOnHand() {
        stock.setOnHandQuantity(10);
        when(variantStockRepository.findForUpdate(any(), any())).thenReturn(Optional.of(stock));

        stockAllocationService.recordSale(productVariant, warehouse, 4, 99L);

        assertEquals(6, stock.getOnHandQuantity());
    }

    @Test
    void recordSale_withInsufficientStock_throws_andLeavesStockUnchanged() {
        stock.setOnHandQuantity(2);
        when(variantStockRepository.findForUpdate(any(), any())).thenReturn(Optional.of(stock));

        assertThrows(IllegalStateException.class, () ->
            stockAllocationService.recordSale(productVariant, warehouse, 5, 99L));

        assertEquals(2, stock.getOnHandQuantity()); // confirms the throw happens before any mutation
    }
}