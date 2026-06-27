package com.nikos.retail.inventory;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public class IncomingStockRequest {

    @NotNull
    private Long variantId;

    @NotNull
    private Long locationId;

    @NotNull
    @Positive
    private Integer expectedQuantity;

    private LocalDate expectedDate;

    public Long getVariantId(){return variantId;}
    public void setVariantId(Long variantId){this.variantId = variantId;}

    public Long getLocationId(){return locationId;}
    public void setLocationId(Long locationId){this.locationId = locationId;}

    public Integer getExpectedQuantity(){return expectedQuantity;}
    public void setExpectedQuantity(Integer expectedQuantity){this.expectedQuantity = expectedQuantity;}

    public LocalDate getExpectedDate(){return expectedDate;}
    public void setExpectedDate(LocalDate expectedDate){this.expectedDate = expectedDate;}
}