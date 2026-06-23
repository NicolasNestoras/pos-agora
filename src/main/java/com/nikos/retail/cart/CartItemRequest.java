package com.nikos.retail.cart;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CartItemRequest {

    @NotNull(message = "Product variant id is required.")
    private Long productVariantId;

    @Positive(message = "Quantity must be positive.")
    private int quantity;

    public Long getProductVariantId(){
        return productVariantId;
    }

    public void setProductVariantId(Long productVariantId){
        this.productVariantId = productVariantId;
    }

    public int getQuantity(){
        return quantity;
    }

    public void setQuantity(int quantity){
        this.quantity = quantity;
    }
    
}
