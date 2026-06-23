package com.nikos.retail.sale;

import org.springframework.stereotype.Service;

import com.nikos.retail.cart.*;
import com.nikos.retail.common.exception.ResourceNotFoundException;
import com.nikos.retail.productvariant.ProductVariant;
import com.nikos.retail.productvariant.ProductVariantRepository;

import jakarta.transaction.Transactional;

@Service
public class SaleService {
    
    private final SaleRepository saleRepository;
    private final CartRepository cartRepository;
    private final ProductVariantRepository productVariantRepository;

    public SaleService(SaleRepository saleRepository, CartRepository cartRepository, ProductVariantRepository productVariantRepository){
        this.saleRepository = saleRepository;
        this.cartRepository = cartRepository;
        this.productVariantRepository = productVariantRepository;
    }

    @Transactional
    public SaleResponse checkout(SaleRequest request) {
        Cart cart = cartRepository.findById(request.getCartId())
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found with id: " + request.getCartId()));

        if (cart.getStatus() == CartStatus.CHECKED_OUT) {
            throw new IllegalStateException("Cart has already been checked out");
        }
        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot checkout an empty cart");
        }

        Sale sale = new Sale();
        sale.setCustomer(cart.getCustomer());

        for (CartItem cartItem : cart.getItems()) {
            ProductVariant variant = cartItem.getProductVariant();

            // confirm there's enough stock before committing
            if (variant.getStockQuantity() < cartItem.getQuantity()) {
                throw new IllegalStateException(
                    "Insufficient stock for SKU " + variant.getSku()
                    + " (available: " + variant.getStockQuantity()
                    + ", requested: " + cartItem.getQuantity() + ")");
            }

            SaleItem saleItem = new SaleItem();
            saleItem.setSale(sale);
            saleItem.setProductVariant(variant);
            saleItem.setQuantity(cartItem.getQuantity());
            saleItem.setUnitPrice(cartItem.getUnitPrice()); // carry over the snapshotted price
            sale.getItems().add(saleItem);

            // deduct stock
            variant.setStockQuantity(variant.getStockQuantity() - cartItem.getQuantity());
            productVariantRepository.save(variant);
        }

        Sale savedSale = saleRepository.save(sale);

        cart.setStatus(CartStatus.CHECKED_OUT);
        cartRepository.save(cart);

        return SaleResponse.fromEntity(savedSale);
    }

    public SaleResponse getSaleById(Long id) {
        Sale sale = saleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Sale not found with id: " + id));
        return SaleResponse.fromEntity(sale);
    }
}
