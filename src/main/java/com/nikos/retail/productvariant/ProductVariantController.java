package com.nikos.retail.productvariant;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/products/{productId}/variants")
public class ProductVariantController {
    private final ProductVariantService productVariantService;

    public ProductVariantController(ProductVariantService productVariantService) {
        this.productVariantService = productVariantService;
    }

    @GetMapping
    public List<ProductVariantResponse> getVariants(@PathVariable Long productId) {
        return productVariantService.getVariantsForProduct(productId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductVariantResponse addVariant(@PathVariable Long productId,
                                              @Valid @RequestBody ProductVariantRequest request) {
        return productVariantService.addVariant(productId, request);
    }    

    @PostMapping("/{variantId}/prices")
    public ResponseEntity<Void> setPrice(@PathVariable Long productId,
                                        @PathVariable Long variantId,
                                        @Valid @RequestBody ProductVariantPriceRequest request) {
        productVariantService.setVariantPrice(variantId, request);
        return ResponseEntity.ok().build();
    }
}
