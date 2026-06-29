package com.nikos.retail.inventory;

import com.nikos.retail.order.Order;
import com.nikos.retail.productvariant.ProductVariant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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

    @Test
    void commitReservations_decrementsonHand_andMarksCommited(){
        Reservation reservation = new Reservation();
        reservation.setProductVariant(productVariant);
        reservation.setLocation(warehouse);
        reservation.setOrder(order);
        reservation.setQuantity(4);
        reservation.setStatus(ReservationStatus.COMMITTED);

        stock.setOnHandQuantity(10);
        stock.setReservedQuantity(4);
        
        when(reservationRepository.findByOrder_IdAndStatus(any(), eq(ReservationStatus.ACTIVE)))
            .thenReturn(List.of(reservation));
        when(variantStockRepository.findForUpdate(any(), any())).thenReturn(Optional.of(stock));

        stockAllocationService.commitReservations(order);

        assertEquals(6, stock.getOnHandQuantity());
        assertEquals(0, stock.getReservedQuantity());
        assertEquals(ReservationStatus.COMMITTED, reservation.getStatus());
    }


    @Test
    void releaseReservations_restoresReserved_onHandUnchanged_cancelsPendingBackorders() {
        Reservation reservation = new Reservation();
        reservation.setProductVariant(productVariant);
        reservation.setLocation(warehouse);
        reservation.setOrder(order);
        reservation.setQuantity(3);
        reservation.setStatus(ReservationStatus.ACTIVE);

        Backorder backorder = new Backorder();
        backorder.setProductVariant(productVariant);
        backorder.setOrder(order);
        backorder.setQuantity(2);
        backorder.setStatus(BackorderStatus.PENDING);

        stock.setOnHandQuantity(10);
        stock.setReservedQuantity(3);

        when(reservationRepository.findByOrder_IdAndStatus(any(), eq(ReservationStatus.ACTIVE)))
            .thenReturn(List.of(reservation));
        when(variantStockRepository.findForUpdate(any(), any())).thenReturn(Optional.of(stock));
        when(backorderRepository.findByOrder_IdAndStatus(any(), eq(BackorderStatus.PENDING)))
            .thenReturn(List.of(backorder));

        stockAllocationService.releaseReservations(order);

        assertEquals(10, stock.getOnHandQuantity()); // never touched — nothing physically moved
        assertEquals(0, stock.getReservedQuantity());
        assertEquals(ReservationStatus.RELEASED, reservation.getStatus());
        assertEquals(BackorderStatus.CANCELLED, backorder.getStatus());
    }


    @Test
    void fulfillBackorder_fullAmount_marksFulfilled() {
        Backorder backorder = new Backorder();
        backorder.setProductVariant(productVariant);
        backorder.setOrder(order);
        backorder.setQuantity(5);
        backorder.setStatus(BackorderStatus.PENDING);

        stock.setOnHandQuantity(10);
        when(variantStockRepository.findForUpdate(any(), any())).thenReturn(Optional.of(stock));

        stockAllocationService.fulfillBackorder(backorder, warehouse, 5);

        assertEquals(0, backorder.getQuantity());
        assertEquals(BackorderStatus.FULFILLED, backorder.getStatus());
        assertEquals(5, stock.getReservedQuantity());
    }

    @Test
    void fulfillBackorder_partialAmount_staysPending() {
        Backorder backorder = new Backorder();
        backorder.setProductVariant(productVariant);
        backorder.setOrder(order);
        backorder.setQuantity(5);
        backorder.setStatus(BackorderStatus.PENDING);

        stock.setOnHandQuantity(10);
        when(variantStockRepository.findForUpdate(any(), any())).thenReturn(Optional.of(stock));

        stockAllocationService.fulfillBackorder(backorder, warehouse, 3);

        assertEquals(2, backorder.getQuantity());
        assertEquals(BackorderStatus.PENDING, backorder.getStatus());
    }

    @Test
    void fulfillBackorder_throws_whenQuantityExceedsBackorderRemaining() {
        Backorder backorder = new Backorder();
        backorder.setProductVariant(productVariant);
        backorder.setOrder(order);
        backorder.setQuantity(3);
        backorder.setStatus(BackorderStatus.PENDING);

        assertThrows(IllegalArgumentException.class, () ->
            stockAllocationService.fulfillBackorder(backorder, warehouse, 5));
    }

    @Test
    void fulfillBackorder_throws_whenInsufficientAvailableStock() {
        Backorder backorder = new Backorder();
        backorder.setProductVariant(productVariant);
        backorder.setOrder(order);
        backorder.setQuantity(5);
        backorder.setStatus(BackorderStatus.PENDING);

        stock.setOnHandQuantity(2);
        when(variantStockRepository.findForUpdate(any(), any())).thenReturn(Optional.of(stock));

        assertThrows(IllegalStateException.class, () ->
            stockAllocationService.fulfillBackorder(backorder, warehouse, 5));
    }
}