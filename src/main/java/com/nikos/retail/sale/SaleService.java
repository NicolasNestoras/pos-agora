package com.nikos.retail.sale;

import org.springframework.stereotype.Service;

import com.nikos.retail.cart.*;
import com.nikos.retail.common.exception.ResourceNotFoundException;
import com.nikos.retail.inventory.InventoryMovementType;
import com.nikos.retail.inventory.InventoryService;
import com.nikos.retail.productvariant.ProductVariant;
import com.nikos.retail.productvariant.ProductVariantRepository;

import jakarta.transaction.Transactional;

@Service
public class SaleService {
    
    private final SaleRepository saleRepository;
    private final CartRepository cartRepository;    private final InventoryService inventoryService;

    public SaleService(SaleRepository saleRepository, CartRepository cartRepository, InventoryService inventoryService){
        this.saleRepository = saleRepository;
        this.cartRepository = cartRepository;
        this.inventoryService = inventoryService;
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
            ProductVariant productVariant = cartItem.getProductVariant();

            // confirm there's enough stock before committing
            if (productVariant.getStockQuantity() < cartItem.getQuantity()) {
                throw new IllegalStateException(
                    "Insufficient stock for SKU " + productVariant.getSku()
                    + " (available: " + productVariant.getStockQuantity()
                    + ", requested: " + cartItem.getQuantity() + ")");
            }

            SaleItem saleItem = new SaleItem();
            saleItem.setSale(sale);
            saleItem.setProductVariant(productVariant);
            saleItem.setQuantity(cartItem.getQuantity());
            saleItem.setUnitPrice(cartItem.getUnitPrice()); // carry over the snapshotted price
            sale.getItems().add(saleItem);
        }

        Sale savedSale = saleRepository.save(sale);

        for (SaleItem saleItem: savedSale.getItems()){
            // Update Stock
            inventoryService.recordMovement(saleItem.getProductVariant().getId(), InventoryMovementType.SALE, -saleItem.getQuantity(), savedSale.getId(), "Sale Checkout.");

        }

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
