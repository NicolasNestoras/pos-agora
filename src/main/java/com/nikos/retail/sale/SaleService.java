package com.nikos.retail.sale;

import org.springframework.stereotype.Service;

import com.nikos.retail.cart.*;
import com.nikos.retail.common.exception.ResourceNotFoundException;
import com.nikos.retail.inventory.Location;
import com.nikos.retail.inventory.LocationRepository;
import com.nikos.retail.inventory.LocationType;
import com.nikos.retail.inventory.StockAllocationService;
import com.nikos.retail.productvariant.ProductVariant;

import jakarta.transaction.Transactional;

@Service
public class SaleService {
    
    private final SaleRepository saleRepository;
    private final CartRepository cartRepository;  
    private final LocationRepository locationRepository;
    private final StockAllocationService stockAllocationService;

    public SaleService(SaleRepository saleRepository, CartRepository cartRepository, 
        LocationRepository locationRepository, StockAllocationService stockAllocationService){
        this.saleRepository = saleRepository;
        this.cartRepository = cartRepository;
        this.locationRepository = locationRepository;
        this.stockAllocationService = stockAllocationService;
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
        
        Location store = locationRepository.findByType(LocationType.STORE)
            .stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No store location is configured"));

        Sale sale = new Sale();
        sale.setCustomer(cart.getCustomer());
        sale.setLocation(store);

        for (CartItem cartItem : cart.getItems()) {
            ProductVariant productVariant = cartItem.getProductVariant();

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
            stockAllocationService.recordSale(saleItem.getProductVariant(), store, 
            saleItem.getQuantity(), savedSale.getId());
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
