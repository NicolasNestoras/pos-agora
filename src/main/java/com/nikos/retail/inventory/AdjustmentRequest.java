package com.nikos.retail.inventory;

import jakarta.validation.constraints.NotNull;

public class AdjustmentRequest {

    @NotNull(message = "Quantity change cannot be empty.")
    private int quantityChange;

    private String note;

    public AdjustmentRequest(){}

    public int getQuantityChange(){return quantityChange;}
    public void setQuantityChange(int quantityChange){this.quantityChange = quantityChange;}

    public String getNote(){return note;}
    public void setNote(String note){this.note = note;}
    
}
