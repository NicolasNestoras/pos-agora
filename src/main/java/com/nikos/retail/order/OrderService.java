package com.nikos.retail.order;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nikos.retail.cart.Cart;
import com.nikos.retail.cart.CartItem;
import com.nikos.retail.cart.CartRepository;
import com.nikos.retail.cart.CartStatus;
import com.nikos.retail.common.exception.ResourceNotFoundException;
import com.nikos.retail.inventory.InventoryMovementType;
import com.nikos.retail.inventory.InventoryService;
import com.nikos.retail.productvariant.ProductVariant;
import com.nikos.retail.productvariant.ProductVariantRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final InventoryService inventoryService;

    public OrderService(OrderRepository orderRepository,
                         CartRepository cartRepository, 
                        InventoryService inventoryService) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.inventoryService = inventoryService;
    }

    @Transactional
    public OrderResponse placeOrder(OrderRequest request) {
        Cart cart = cartRepository.findById(request.getCartId())
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found with id: " + request.getCartId()));

        if (cart.getCustomer() == null) {
            throw new IllegalStateException("An order requires a known customer - cart has none attached");
        }
        if (cart.getStatus() == CartStatus.CHECKED_OUT) {
            throw new IllegalStateException("Cart has already been checked out");
        }
        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot place an order from an empty cart");
        }

        Order order = new Order();
        order.setCustomer(cart.getCustomer());
        order.setShippingAddress(request.getShippingAddress());

        for (CartItem cartItem : cart.getItems()) {
            ProductVariant variant = cartItem.getProductVariant();

            if (variant.getStockQuantity() < cartItem.getQuantity()) {
                throw new IllegalStateException(
                    "Insufficient stock for SKU " + variant.getSku()
                    + " (available: " + variant.getStockQuantity()
                    + ", requested: " + cartItem.getQuantity() + ")");
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductVariant(variant);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(cartItem.getUnitPrice());
            order.getItems().add(orderItem);

        }

        Order savedOrder = orderRepository.save(order);

        for (OrderItem orderItem: savedOrder.getItems()){
            inventoryService.recordMovement(orderItem.getProductVariant().getId(), InventoryMovementType.ORDER, -orderItem.getQuantity(), savedOrder.getId(), "Order checkout.");
        }

        cart.setStatus(CartStatus.CHECKED_OUT);
        cartRepository.save(cart);

        return OrderResponse.fromEntity(savedOrder);
    }

    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        return OrderResponse.fromEntity(order);
    }

    public List<OrderResponse> getOrdersForCustomer(Long customerId) {
        return orderRepository.findByCustomerId(customerId)
            .stream()
            .map(OrderResponse::fromEntity)
            .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse updateStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        validateStatusTransition(order.getStatus(), newStatus);
        order.setStatus(newStatus);

        Order saved = orderRepository.save(order);
        return OrderResponse.fromEntity(saved);
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        boolean valid = switch (current) {
            case PENDING -> next == OrderStatus.PAID || next == OrderStatus.CANCELLED;
            case PAID -> next == OrderStatus.SHIPPED || next == OrderStatus.CANCELLED;
            case SHIPPED -> next == OrderStatus.DELIVERED;
            case DELIVERED, CANCELLED -> false; // terminal states, no further transitions
        };

        if (!valid) {
            throw new IllegalStateException(
                "Cannot transition order from " + current + " to " + next);
        }
    }
}