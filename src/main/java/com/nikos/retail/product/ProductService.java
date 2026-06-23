package com.nikos.retail.product;

import org.springframework.stereotype.Service;

import com.nikos.retail.common.exception.ResourceNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
            .stream()
            .map(ProductResponse::fromEntity)
            .collect(Collectors.toList());
    }

    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        return ProductResponse.fromEntity(product);
    }

    public ProductResponse createProduct(ProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setCategory(request.getCategory());
        Product saved = productRepository.save(product);
        return ProductResponse.fromEntity(saved);
    }
}