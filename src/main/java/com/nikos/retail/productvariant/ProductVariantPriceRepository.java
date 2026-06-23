package com.nikos.retail.productvariant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductVariantPriceRepository extends JpaRepository<ProductVariantPrice, Long> {
    Optional<ProductVariantPrice> findByProductVariantIdAndPriceListId(Long productVariantId, Long priceListId);
}