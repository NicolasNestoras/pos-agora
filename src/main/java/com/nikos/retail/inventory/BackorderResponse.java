package com.nikos.retail.inventory;

import java.time.OffsetDateTime;

public class BackorderResponse {
    private Long id;
    private Long variantId;
    private Long orderId;
    private int quantity;
    private BackorderStatus status;
    private OffsetDateTime createdAt;

    public BackorderResponse(){}

    public static BackorderResponse fromEntity(Backorder backorder){
        BackorderResponse dto = new BackorderResponse();
        dto.id = backorder.getId();
        dto.variantId = backorder.getProductVariant().getId();
        dto.orderId = backorder.getOrder().getId();
        dto.quantity = backorder.getQuantity();
        dto.status = backorder.getStatus();
        dto.createdAt = backorder.getCreatedAt();
        return dto;
    }

    public Long getId(){return id;}
    public Long getVariantId(){return variantId;}
    public Long getOrderId(){return orderId;}
    public int getQuantity(){return quantity;}
    public BackorderStatus getStatus(){return status;}
    public OffsetDateTime getCreatedAt(){return createdAt;}
}   