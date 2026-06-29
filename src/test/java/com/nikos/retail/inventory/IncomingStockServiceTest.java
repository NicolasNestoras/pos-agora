package com.nikos.retail.inventory;

import com.nikos.retail.productvariant.ProductVariant;
import com.nikos.retail.productvariant.ProductVariantRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncomingStockServiceTest {

    @Mock private IncomingStockRepository incomingStockRepository;
    @Mock private VariantStockRepository variantStockRepository;
    @Mock private StockMovementRepository stockMovementRepository;
    @Mock private BackorderRepository backorderRepository;
    @Mock private StockAllocationService stockAllocationService;
    @Mock private ProductVariantRepository productVariantRepository;
    @Mock private LocationRepository locationRepository;

    @InjectMocks
    private IncomingStockService incomingStockService;

    private ProductVariant variant;
    private Location warehouse;
    private IncomingStock incomingStock;
    private VariantStock stock;

    @BeforeEach
    void setUp() {
        variant = new ProductVariant();
        variant.setSku("SKU-1");

        warehouse = new Location();
        warehouse.setName("Main Warehouse");
        warehouse.setType(LocationType.WAREHOUSE);

        incomingStock = new IncomingStock();
        incomingStock.setProductVariant(variant);
        incomingStock.setLocation(warehouse);
        incomingStock.setExpectedQuantity(10); // the original promise

        stock = new VariantStock();
        stock.setProductVariant(variant);
        stock.setLocation(warehouse);
    }

    @Test
    void receive_restocksByActualQuantity_notExpectedQuantity() {
        stock.setOnHandQuantity(5);

        when(incomingStockRepository.findById(1L)).thenReturn(Optional.of(incomingStock));
        when(variantStockRepository.findForUpdate(any(), any())).thenReturn(Optional.of(stock));
        when(backorderRepository.sumPendingQuantity(any(), eq(BackorderStatus.PENDING))).thenReturn(0);
        when(incomingStockRepository.save(any(IncomingStock.class))).thenAnswer(inv -> inv.getArgument(0));

        ReceiveIncomingStockRequest request = new ReceiveIncomingStockRequest();
        request.setActualQuantity(7);
        // Expected 10, supplier shorted to 7.
        IncomingStockResponse response = incomingStockService.receive(1L, request);

        assertEquals(12, stock.getOnHandQuantity()); // 5 + 7, not 5 + 10
        assertEquals(7, response.getReceivedQuantity());
        assertEquals(10, response.getExpectedQuantity()); // original promise still visible
    }

    @Test
    void receive_overshipment_stillFulfillsBackordersCorrectly() {
        stock.setOnHandQuantity(0);

        Backorder backorder = new Backorder();
        backorder.setProductVariant(variant);
        backorder.setQuantity(5);
        backorder.setStatus(BackorderStatus.PENDING);

        when(incomingStockRepository.findById(1L)).thenReturn(Optional.of(incomingStock));
        when(variantStockRepository.findForUpdate(any(), any())).thenReturn(Optional.of(stock));
        when(backorderRepository.sumPendingQuantity(any(), eq(BackorderStatus.PENDING))).thenReturn(5);
        when(backorderRepository.findByProductVariant_IdAndStatusOrderByCreatedAtAsc(any(), eq(BackorderStatus.PENDING)))
            .thenReturn(List.of(backorder));
        when(incomingStockRepository.save(any(IncomingStock.class))).thenAnswer(inv -> inv.getArgument(0));

        ReceiveIncomingStockRequest request = new ReceiveIncomingStockRequest();
        request.setActualQuantity(12);
        // Expected 10, supplier sent 12 by mistake — still covers the 5 backordered.
        incomingStockService.receive(1L, request);

        verify(stockAllocationService).fulfillBackorder(backorder, warehouse, 5);
    }

    @Test
    void receive_shortfall_doesNotAutoFulfill() {
        stock.setOnHandQuantity(0);

        when(incomingStockRepository.findById(1L)).thenReturn(Optional.of(incomingStock));
        when(variantStockRepository.findForUpdate(any(), any())).thenReturn(Optional.of(stock));
        when(backorderRepository.sumPendingQuantity(any(), eq(BackorderStatus.PENDING))).thenReturn(5);
        when(incomingStockRepository.save(any(IncomingStock.class))).thenAnswer(inv -> inv.getArgument(0));
        
        ReceiveIncomingStockRequest request = new ReceiveIncomingStockRequest();
        request.setActualQuantity(3);
        incomingStockService.receive(1L, request);

        verify(stockAllocationService, never()).fulfillBackorder(any(), any(), anyInt());
    }

    @Test
    void receive_throws_whenAlreadyReceived() {
        incomingStock.setStatus(IncomingStockStatus.RECEIVED);
        when(incomingStockRepository.findById(1L)).thenReturn(Optional.of(incomingStock));

        ReceiveIncomingStockRequest request = new ReceiveIncomingStockRequest();
        request.setActualQuantity(10);
        assertThrows(IllegalStateException.class, () -> incomingStockService.receive(1L, request));
    }
}