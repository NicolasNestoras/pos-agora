package com.nikos.retail.sale;

import jakarta.validation.constraints.NotNull;

public class SaleRequest {

    @NotNull
    private Long cartId;


    public Long getCartId(){return cartId;}
    public void setCartId(Long cartId){this.cartId = cartId;}

}
