package com.nikos.retail.inventory;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.nikos.retail.common.exception.ResourceNotFoundException;
import com.nikos.retail.productvariant.ProductVariant;
import com.nikos.retail.productvariant.ProductVariantRepository;

import jakarta.transaction.Transactional;



@Service
public class InventoryService {
    
    private final InventoryMovementRepository inventoryMovementRepository;
    private final ProductVariantRepository productVariantRepository;

    public InventoryService(InventoryMovementRepository inventoryMovementRepository,
        ProductVariantRepository productVariantRepository){
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.productVariantRepository = productVariantRepository;
    }

    @Transactional
    public InventoryMovement recordMovement(Long productVariantId, InventoryMovementType type, 
        int quantityChange, Long referenceId, String note){
        
        ProductVariant productVariant = productVariantRepository.findById(productVariantId)
            .orElseThrow(()->new ResourceNotFoundException("ProductVariant doesn't exist with id: "+ productVariantId));
    
        int newStock = productVariant.getStockQuantity()+quantityChange;
        if (newStock<0){
            throw new IllegalStateException("Movement will result in negative stock.");
        }

        InventoryMovement movement = new InventoryMovement();
        movement.setReferenceId(referenceId);
        movement.setProductVariant(productVariant);
        movement.setQuantityChange(quantityChange);
        movement.setType(type);
        movement.setNote(note);
        inventoryMovementRepository.save(movement);

        productVariant.setStockQuantity(newStock);
        productVariantRepository.save(productVariant);
        return movement;
    }

    @Transactional
    public InventoryMovementResponse adjustStock(Long productVariantId, AdjustmentRequest request){
        InventoryMovement movement = recordMovement(productVariantId, InventoryMovementType.ADJUSTMENT, request.getQuantityChange(), null, request.getNote());

        return InventoryMovementResponse.fromEntity(movement);

    }
    public List<InventoryMovementResponse> getHistoryForVariant(Long productVariantId){
        return inventoryMovementRepository.findByProductVariantIdOrderByCreatedAtDesc(productVariantId)
            .stream()
            .map(InventoryMovementResponse::fromEntity)
            .collect(Collectors.toList());
    }


}
