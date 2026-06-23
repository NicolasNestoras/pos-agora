package com.nikos.retail.sale;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class SaleResponse {

    private Long id;
    private Long customerId;
    private SaleStatus status;
    private OffsetDateTime createdAt;
    private List<SaleItemResponse> items;
    private BigDecimal total;

    public SaleResponse() {}

    public static SaleResponse fromEntity(Sale sale) {
        SaleResponse dto = new SaleResponse();
        dto.id = sale.getId();
        dto.customerId = sale.getCustomer() != null ? sale.getCustomer().getId() : null;
        dto.status = sale.getStatus();
        dto.createdAt = sale.getCreatedAt();
        dto.items = sale.getItems()
            .stream()
            .map(SaleItemResponse::fromEntity)
            .collect(Collectors.toList());
        dto.total = sale.getTotal();
        return dto;
    }

    public Long getId() {
        return id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public SaleStatus getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public List<SaleItemResponse> getItems() {
        return items;
    }

    public BigDecimal getTotal() {
        return total;
    }
}