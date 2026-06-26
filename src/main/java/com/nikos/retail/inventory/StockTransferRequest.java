package com.nikos.retail.inventory;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class StockTransferRequest {

    @NotNull
    private Long variantId;

    @NotNull
    private Long fromLocationId;

    @NotNull
    private Long toLocationId;

    @NotNull
    @Positive
    private Integer quantity;

    public Long getVariantId(){return variantId;}
    public void setVariantId(Long variantId){this.variantId = variantId;}

    public Long getFromLocationId(){return fromLocationId;}
    public void setFromLocationId(Long fromLocationId){this.fromLocationId = fromLocationId;}

    public Long getToLocationId(){return toLocationId;}
    public void setToLocationId(Long toLocationId){this.toLocationId = toLocationId;}

    public Integer getQuantity(){return quantity;}
    public void setQuantity(Integer quantity){this.quantity = quantity;}
}