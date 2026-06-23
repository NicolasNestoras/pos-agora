package com.nikos.retail.cart;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class CartResponse {
    private Long id;
    private Long customerId;
    private CartStatus status;
    private OffsetDateTime createdAt;
    private List<CartItemResponse> items;
    private BigDecimal total;

    public CartResponse(){}
    
    public static CartResponse fromEntity(Cart cart){
        CartResponse dto= new CartResponse();
        dto.id = cart.getId();
        dto.customerId = cart.getCustomer() != null ? cart.getCustomer().getId():null;
        dto.status = cart.getStatus();
        dto.createdAt = cart.getCreatedAt();
        dto.items = cart.getItems()
            .stream()
            .map(CartItemResponse::fromEntity)
            .collect(Collectors.toList());
        dto.total = dto.items.stream()
            .map(CartItemResponse::getLineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return dto;
    }

    public Long getId() {
        return id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public CartStatus getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public List<CartItemResponse> getItems() {
        return items;
    }

    public BigDecimal getTotal() {
        return total;
    }
}
