package com.nikos.retail.productvariant;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public class ProductVariantRequest {
    
    @NotBlank(message = "SKU cannot be blank.")
    private String sku;

    @NotBlank(message = "Size cannot be blank")
    private String size;

    private String color;

    @Positive(message = "Price must be positive.")
    private BigDecimal price;

    @PositiveOrZero(message = "Stock cannot be negative.")
    private int stockQuantity;

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }
}
