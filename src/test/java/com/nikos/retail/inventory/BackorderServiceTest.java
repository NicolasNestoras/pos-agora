package com.nikos.retail.inventory;

import com.nikos.retail.order.Order;
import com.nikos.retail.productvariant.ProductVariant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BackorderServiceTest {

    @Mock private BackorderRepository backorderRepository;
    @Mock private StockAllocationService stockAllocationService;
    @Mock private LocationRepository locationRepository;

    @InjectMocks
    private BackorderService backorderService;

    private Backorder backorder;
    private Location warehouse;

    @BeforeEach
    void setUp() {
        ProductVariant variant = new ProductVariant();
        variant.setSku("SKU-1");

        backorder = new Backorder();
        backorder.setProductVariant(variant);
        backorder.setOrder(new Order());
        backorder.setQuantity(5);
        backorder.setStatus(BackorderStatus.PENDING);

        warehouse = new Location();
        warehouse.setType(LocationType.WAREHOUSE);
    }

    @Test
    void fulfill_delegatesToStockAllocationService_atWarehouse() {
        when(backorderRepository.findById(1L)).thenReturn(Optional.of(backorder));
        when(locationRepository.findByType(LocationType.WAREHOUSE)).thenReturn(List.of(warehouse));

        BackorderFulfillRequest request = new BackorderFulfillRequest();
        request.setQuantity(3);

        backorderService.fulfill(1L, request);

        verify(stockAllocationService).fulfillBackorder(backorder, warehouse, 3);
    }

    @Test
    void fulfill_throws_whenBackorderNotPending() {
        backorder.setStatus(BackorderStatus.FULFILLED);
        when(backorderRepository.findById(1L)).thenReturn(Optional.of(backorder));

        BackorderFulfillRequest request = new BackorderFulfillRequest();
        request.setQuantity(1);

        assertThrows(IllegalStateException.class, () -> backorderService.fulfill(1L, request));
    }

    @Test
    void fulfill_throws_whenNoWarehouseConfigured() {
        when(backorderRepository.findById(1L)).thenReturn(Optional.of(backorder));
        when(locationRepository.findByType(LocationType.WAREHOUSE)).thenReturn(List.of());

        BackorderFulfillRequest request = new BackorderFulfillRequest();
        request.setQuantity(1);

        assertThrows(IllegalStateException.class, () -> backorderService.fulfill(1L, request));
    }
}