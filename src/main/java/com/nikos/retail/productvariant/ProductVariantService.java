package com.nikos.retail.productvariant;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.nikos.retail.common.exception.ResourceNotFoundException;
import com.nikos.retail.product.Product;
import com.nikos.retail.product.ProductRepository;

@Service
public class ProductVariantService {
    
    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;
    private final ProductVariantPriceRepository productVariantPriceRepository;
    private final PriceListRepository priceListRepository;

    public ProductVariantService(ProductVariantRepository productVariantRepository,
                                  ProductRepository productRepository, ProductVariantPriceRepository productVariantPriceRepository, PriceListRepository priceListRepository) {
        this.productVariantRepository = productVariantRepository;
        this.productRepository = productRepository;
        this.productVariantPriceRepository = productVariantPriceRepository;
        this.priceListRepository = priceListRepository;
    }

    public List<ProductVariantResponse> getVariantsForProduct(long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        return product.getVariants()
            .stream()
            .map(ProductVariantResponse::fromEntity)
            .collect(Collectors.toList());
    }

    public ProductVariantResponse addVariant(long productId, ProductVariantRequest request) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        ProductVariant variant = new ProductVariant();
        variant.setSku(request.getSku());
        variant.setSize(request.getSize());
        variant.setColor(request.getColor());
        variant.setPrice(request.getPrice());
        variant.setProduct(product); // links variant to its parent product

        ProductVariant saved = productVariantRepository.save(variant);
        return ProductVariantResponse.fromEntity(saved);
    }

    public void setVariantPrice(Long variantId, ProductVariantPriceRequest request) {
        ProductVariant variant = productVariantRepository.findById(variantId)
            .orElseThrow(() -> new ResourceNotFoundException("Variant not found with id: " + variantId));

        PriceList priceList = priceListRepository.findById(request.getPriceListId())
            .orElseThrow(() -> new ResourceNotFoundException("Price list not found with id: " + request.getPriceListId()));

        ProductVariantPrice productVariantPrice = productVariantPriceRepository
            .findByProductVariantIdAndPriceListId(variantId, priceList.getId())
            .orElse(new ProductVariantPrice());

        productVariantPrice.setProductVariant(variant);
        productVariantPrice.setPriceList(priceList);
        productVariantPrice.setPrice(request.getPrice());

        productVariantPriceRepository.save(productVariantPrice);
    }
}
