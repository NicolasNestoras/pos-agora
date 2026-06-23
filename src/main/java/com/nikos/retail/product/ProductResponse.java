package com.nikos.retail.product;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.nikos.retail.productvariant.ProductVariantResponse;

public class ProductResponse {
    private long id;
    private String name;
    private String category;
    private OffsetDateTime createdAt;
    private List<ProductVariantResponse> variants;

    public ProductResponse() {}

    public static ProductResponse fromEntity(Product product) {
        ProductResponse dto = new ProductResponse();
        dto.id = product.getId();
        dto.name = product.getName();
        dto.category = product.getCategory();
        dto.createdAt = product.getCreatedAt();
        dto.variants = product.getVariants()
                               .stream()
                               .map(ProductVariantResponse::fromEntity)
                               .collect(Collectors.toList()); 
    return dto;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public List<ProductVariantResponse> getVariants(){
        return variants;
    }
}
