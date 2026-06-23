package com.nikos.retail.order;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class OrderResponse {

    private Long id;
    private Long customerId;
    private OrderStatus status;
    private String shippingAddress;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private List<OrderItemResponse> items;
    private BigDecimal total;

    public OrderResponse() {}

    public static OrderResponse fromEntity(Order order) {
        OrderResponse dto = new OrderResponse();
        dto.id = order.getId();
        dto.customerId = order.getCustomer().getId();
        dto.status = order.getStatus();
        dto.shippingAddress = order.getShippingAddress();
        dto.createdAt = order.getCreatedAt();
        dto.updatedAt = order.getUpdatedAt();
        dto.items = order.getItems()
            .stream()
            .map(OrderItemResponse::fromEntity)
            .collect(Collectors.toList());
        dto.total = order.getTotal();
        return dto;
    }

    public Long getId() {
        return id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<OrderItemResponse> getItems() {
        return items;
    }

    public BigDecimal getTotal() {
        return total;
    }
}
