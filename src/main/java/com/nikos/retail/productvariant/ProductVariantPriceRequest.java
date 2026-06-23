package com.nikos.retail.productvariant;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class ProductVariantPriceRequest {

    @NotNull(message = "Price list id is required")
    private Long priceListId;

    @Positive(message = "Price must be greater than zero")
    private BigDecimal price;

    public ProductVariantPriceRequest() {}

    public Long getPriceListId() {
        return priceListId;
    }

    public void setPriceListId(Long priceListId) {
        this.priceListId = priceListId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}