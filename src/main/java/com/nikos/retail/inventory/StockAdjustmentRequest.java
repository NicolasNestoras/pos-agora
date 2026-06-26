package com.nikos.retail.inventory;

import jakarta.validation.constraints.NotNull;

public class StockAdjustmentRequest {

    @NotNull
    private Long variantId;

    @NotNull
    private Long locationId;

    @NotNull
    private Integer quantityChange;

    @NotNull
    private StockMovementReason reason;

    public Long getVariantId(){return variantId;}
    public void setVariantId(Long variantId){this.variantId = variantId;}

    public Long getLocationId(){return locationId;}
    public void setLocationId(Long locationId){this.locationId = locationId;}

    public Integer getQuantityChange(){return quantityChange;}
    public void setQuantityChange(Integer quantityChange){this.quantityChange = quantityChange;}

    public StockMovementReason getReason(){return reason;}
    public void setReason(StockMovementReason reason){this.reason = reason;}
}