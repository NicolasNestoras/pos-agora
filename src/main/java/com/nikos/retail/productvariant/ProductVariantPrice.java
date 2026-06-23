package com.nikos.retail.productvariant;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "variant_prices",
       uniqueConstraints = @UniqueConstraint(columnNames = {"product_variant_id", "price_list_id"}))
public class ProductVariantPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "price_list_id", nullable = false)
    private PriceList priceList;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    public ProductVariantPrice() {}

    public Long getId() {
        return id;
    }

    public ProductVariant getProductVariant() {
        return productVariant;
    }

    public void setProductVariant(ProductVariant productVariant) {
        this.productVariant = productVariant;
    }

    public PriceList getPriceList() {
        return priceList;
    }

    public void setPriceList(PriceList priceList) {
        this.priceList = priceList;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}