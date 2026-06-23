package com.nikos.retail.payment;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class PaymentResponse {

    private Long id;
    private Long saleId;
    private Long orderId;
    private PaymentMethod method;
    private PaymentStatus status;
    private BigDecimal amount;
    private OffsetDateTime createdAt;

    public PaymentResponse() {}

    public static PaymentResponse fromEntity(Payment payment) {
        PaymentResponse dto = new PaymentResponse();
        dto.id = payment.getId();
        dto.saleId = payment.getSale() != null ? payment.getSale().getId() : null;
        dto.orderId = payment.getOrder() != null ? payment.getOrder().getId() : null;
        dto.method = payment.getMethod();
        dto.status = payment.getStatus();
        dto.amount = payment.getAmount();
        dto.createdAt = payment.getCreatedAt();
        return dto;
    }

    public Long getId() {
        return id;
    }

    public Long getSaleId() {
        return saleId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}