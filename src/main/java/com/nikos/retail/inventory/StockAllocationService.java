package com.nikos.retail.inventory;

import com.nikos.retail.order.Order;
import com.nikos.retail.productvariant.ProductVariant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StockAllocationService {

    private final VariantStockRepository variantStockRepository;
    private final ReservationRepository reservationRepository;
    private final BackorderRepository backorderRepository;
    private final StockMovementRepository stockMovementRepository;
    private final LowStockEventPublisher lowStockEventPublisher;

    public StockAllocationService(VariantStockRepository variantStockRepository,
                                   ReservationRepository reservationRepository,
                                   BackorderRepository backorderRepository,
                                   StockMovementRepository stockMovementRepository,
                                LowStockEventPublisher lowStockEventPublisher) {
        this.variantStockRepository = variantStockRepository;
        this.reservationRepository = reservationRepository;
        this.backorderRepository = backorderRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.lowStockEventPublisher = lowStockEventPublisher;
    }

    /**
     * Order-specific. allowBackorder=false (retail): throws if there isn't
     * enough available stock. allowBackorder=true (wholesale): reserves
     * what's available, backorders the remainder — never rejected on
     * stock grounds alone. Creates Reservation rows tied to the Order,
     * representing a claim against stock that hasn't shipped yet.
     */

    @Transactional
    public AllocationResult allocate(ProductVariant productVariant, Location location, int requestedQuantity,
                                      boolean allowBackorder, Order order) {

        VariantStock stock = variantStockRepository
            .findForUpdate(productVariant.getId(), location.getId())
            .orElseThrow(() -> new IllegalStateException(
                "No stock record exists for SKU " + productVariant.getSku() + " at " + location.getName()));

        int available = stock.getAvailableQuantity();

        //Retail
        if (!allowBackorder) {
            if (requestedQuantity > available) {
                throw new IllegalStateException(
                    "Insufficient stock for SKU " + productVariant.getSku()
                    + " (available: " + available + ", requested: " + requestedQuantity + ")");
            }
            reserve(stock, location, productVariant, order, requestedQuantity);
            return new AllocationResult(requestedQuantity, 0, false);
        }

        int toReserve = Math.min(requestedQuantity, available);
        int toBackorder = requestedQuantity - toReserve;

        if (toReserve > 0) {
            reserve(stock, location, productVariant, order, toReserve);
        }
        if (toBackorder > 0) {
            Backorder backorder = new Backorder();
            backorder.setProductVariant(productVariant);
            backorder.setOrder(order);
            backorder.setQuantity(toBackorder);
            backorderRepository.save(backorder);
        }

        return new AllocationResult(toReserve, toBackorder, false);
    }

    /**
     * Sale-specific: instant, no pending phase. Throws if there isn't
     * enough on-hand stock at this location; otherwise decrements onHand
     * directly and logs a StockMovement immediately — never a Reservation.
     */
    /**
     * Order reached SHIPPED. Every ACTIVE reservation for this order becomes
     * COMMITTED, and a real StockMovement is written that decreases
     * onHandQuantity — stock leaves now (not at checkout).
     */

    @Transactional
    public void commitReservations(Order order) {
        List<Reservation> reservations =
            reservationRepository.findByOrder_IdAndStatus(order.getId(), ReservationStatus.ACTIVE);

        for (Reservation reservation : reservations) {
            VariantStock stock = variantStockRepository
                .findForUpdate(reservation.getProductVariant().getId(), reservation.getLocation().getId())
                .orElseThrow(() -> new IllegalStateException("Stock record missing during commit"));

            stock.setOnHandQuantity(stock.getOnHandQuantity() - reservation.getQuantity());
            stock.setReservedQuantity(stock.getReservedQuantity() - reservation.getQuantity());
            variantStockRepository.save(stock);

            StockMovement movement = new StockMovement();
            movement.setProductVariant(reservation.getProductVariant());
            movement.setLocation(reservation.getLocation());
            movement.setQuantityChange(-reservation.getQuantity());
            movement.setReason(StockMovementReason.ORDER_FULFILLED);
            movement.setReferenceId(order.getId());
            stockMovementRepository.save(movement);

            reservation.setStatus(ReservationStatus.COMMITTED);
            reservationRepository.save(reservation);
        }
    }

    /**
     * Order was CANCELLED. ACTIVE reservations become RELEASED — reserved
     * quantity goes back down, but onHand never changes, since nothing
     * physically moved. Any still-PENDING backorders for this order are
     * cancelled too, so they don't get fulfilled later for an order that no
     * longer exists.
     */

    @Transactional
    public void releaseReservations(Order order) {
        List<Reservation> reservations =
            reservationRepository.findByOrder_IdAndStatus(order.getId(), ReservationStatus.ACTIVE);

        for (Reservation reservation : reservations) {
            VariantStock stock = variantStockRepository
                .findForUpdate(reservation.getProductVariant().getId(), reservation.getLocation().getId())
                .orElseThrow(() -> new IllegalStateException("Stock record missing during release"));

            stock.setReservedQuantity(stock.getReservedQuantity() - reservation.getQuantity());
            variantStockRepository.save(stock);

            reservation.setStatus(ReservationStatus.RELEASED);
            reservationRepository.save(reservation);
        }

        List<Backorder> backorders =
            backorderRepository.findByOrder_IdAndStatus(order.getId(), BackorderStatus.PENDING);

        for (Backorder backorder : backorders) {
            backorder.setStatus(BackorderStatus.CANCELLED);
            backorderRepository.save(backorder);
        }
    }

    @Transactional
    public void recordSale(ProductVariant productVariant, Location location, int quantity, Long saleId) {
        VariantStock stock = variantStockRepository
            .findForUpdate(productVariant.getId(), location.getId())
            .orElseThrow(() -> new IllegalStateException(
                "No stock record exists for SKU " + productVariant.getSku() + " at " + location.getName()));

        int available = stock.getAvailableQuantity();
        if (quantity > available) {
            throw new IllegalStateException(
                "Insufficient stock for SKU " + productVariant.getSku()
                + " (available: " + available + ", requested: " + quantity + ")");
        }

        stock.setOnHandQuantity(stock.getOnHandQuantity() - quantity);
        variantStockRepository.save(stock);
        lowStockEventPublisher.checkAndPublish(stock); //publish Low Stock event

        StockMovement movement = new StockMovement();
        movement.setProductVariant(productVariant);
        movement.setLocation(location);
        movement.setQuantityChange(-quantity);
        movement.setReason(StockMovementReason.SALE);
        movement.setReferenceId(saleId);
        stockMovementRepository.save(movement);
    }



    private void reserve(VariantStock stock, Location location, ProductVariant productVariant, Order order, int quantity) {
        stock.setReservedQuantity(stock.getReservedQuantity() + quantity);
        variantStockRepository.save(stock);
        lowStockEventPublisher.checkAndPublish(stock); //Publish low stock event

        Reservation reservation = new Reservation();
        reservation.setProductVariant(productVariant);
        reservation.setLocation(location);
        reservation.setOrder(order);
        reservation.setQuantity(quantity);
        reservationRepository.save(reservation);
    }



    @Transactional
    public void fulfillBackorder(Backorder backorder, Location location, int quantity){
        if (quantity> backorder.getQuantity()){
            throw new IllegalArgumentException(
                "Cannot fullfill "+ quantity + " backorder only has "+backorder.getQuantity() + " remaining.");
        }

        VariantStock stock = variantStockRepository
            .findForUpdate(backorder.getProductVariant().getId(), location.getId())
            .orElseThrow(() -> new IllegalStateException(
                "No stock record exists for SKU " + backorder.getProductVariant().getSku()
                + " at " + location.getName()));

        int availableQuantity  = stock.getAvailableQuantity();
        if (quantity > availableQuantity){
            throw new IllegalStateException(
                "Insufficient available stock to fulfill this backorder (available: " + availableQuantity
                + ", requested: " + quantity + ")");
        }
        reserve(stock, location, backorder.getProductVariant(), backorder.getOrder(), quantity);
        int remaining = backorder.getQuantity() - quantity;
        backorder.setQuantity(remaining);
        if (remaining == 0){
            backorder.setStatus(BackorderStatus.FULFILLED);
        }
        backorderRepository.save(backorder);
    }
    
}