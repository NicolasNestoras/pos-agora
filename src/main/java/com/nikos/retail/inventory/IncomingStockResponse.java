package com.nikos.retail.inventory;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public class IncomingStockResponse {
    private Long id;
    private Long variantId;
    private Long locationId;
    private int expectedQuantity;
    private LocalDate expectedDate;
    private IncomingStockStatus status;
    private OffsetDateTime createdAt;

    public IncomingStockResponse(){}

    public static IncomingStockResponse fromEntity(IncomingStock incomingStock){
        IncomingStockResponse dto = new IncomingStockResponse();
        dto.id = incomingStock.getId();
        dto.variantId = incomingStock.getProductVariant().getId();
        dto.locationId = incomingStock.getLocation().getId();
        dto.expectedQuantity = incomingStock.getExpectedQuantity();
        dto.expectedDate = incomingStock.getExpectedDate();
        dto.status = incomingStock.getStatus();
        dto.createdAt = incomingStock.getCreatedAt();
        return dto;
    }

    public Long getId(){return id;}
    public Long getVariantId(){return variantId;}
    public Long getLocationId(){return locationId;}
    public int getExpectedQuantity(){return expectedQuantity;}
    public LocalDate getExpectedDate(){return expectedDate;}
    public IncomingStockStatus getStatus(){return status;}
    public OffsetDateTime getCreatedAt(){return createdAt;}
}