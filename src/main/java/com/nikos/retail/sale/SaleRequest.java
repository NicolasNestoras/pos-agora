package com.nikos.retail.sale;

import jakarta.validation.constraints.NotBlank;

public class SaleRequest {

    @NotBlank
    private Long cartId;


    public Long getCartId(){return cartId;}
    public void setCartId(Long cartId){this.cartId = cartId;}

}
