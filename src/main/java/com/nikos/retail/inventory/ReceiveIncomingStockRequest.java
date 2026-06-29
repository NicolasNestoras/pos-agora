package com.nikos.retail.inventory;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class ReceiveIncomingStockRequest {

    //We make the actual quantity not null, and not zero. If the shipment is a total
    // loss, that something to consider in my design later.
    @NotNull
    @Positive
    private Integer actualQuantity;

    public Integer getActualQuantity(){return actualQuantity;}
    public void setActualQuantity(Integer actualQuantity){this.actualQuantity = actualQuantity;}
}