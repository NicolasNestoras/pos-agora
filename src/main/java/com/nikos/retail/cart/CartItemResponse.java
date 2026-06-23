package com.nikos.retail.cart;

import java.math.BigDecimal;

public class CartItemResponse {

    private Long id;
    private Long productVariantId;
    private String sku;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;

    public CartItemResponse(){}

    public static CartItemResponse fromEntity(CartItem item){
        CartItemResponse dto = new CartItemResponse();
        dto.id = item.getId();
        dto.productVariantId = item.getProductVariant().getId();
        dto.sku = item.getProductVariant().getSku();
        dto.quantity = item.getQuantity();
        dto.unitPrice = item.getUnitPrice();
        dto.lineTotal = item.getLineTotal();
        return dto;
    }

    public Long getId(){ return id;}
    public Long getProductVariantId(){return productVariantId;}
    public String getSku(){return sku;}
    public int getQuantity(){return quantity;}
    public BigDecimal getUnitPrice(){return unitPrice;}
    public BigDecimal getLineTotal(){return lineTotal;}


    
}
