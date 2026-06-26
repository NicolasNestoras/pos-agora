package com.nikos.retail.productvariant;

import java.math.BigDecimal;

public class ProductVariantResponse {
    private long id;
    private String sku;
    private String size;
    private String color;
    private BigDecimal price;
    private int stockQuantity;
    public static ProductVariantResponse fromEntity(ProductVariant variant) {
        ProductVariantResponse dto = new ProductVariantResponse();
        dto.id = variant.getId();
        dto.sku = variant.getSku();
        dto.size = variant.getSize();
        dto.color = variant.getColor();
        dto.price = variant.getPrice();
        return dto;
    }

    public Long getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public String getSize() {
        return size;
    }

    public String getColor() {
        return color;
    }

    public BigDecimal getPrice() {
        return price;
    }
}
