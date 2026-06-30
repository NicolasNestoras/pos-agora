package com.nikos.retail.inventory;

import com.nikos.retail.common.exception.ResourceNotFoundException;
import com.nikos.retail.productvariant.ProductVariant;
import com.nikos.retail.productvariant.ProductVariantRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockTransferService {

    private final StockTransferRepository stockTransferRepository;
    private final VariantStockRepository variantStockRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ProductVariantRepository productVariantRepository;
    private final LocationRepository locationRepository;
    private final LowStockEventPublisher lowStockEventPublisher;

    public StockTransferService(StockTransferRepository stockTransferRepository,
                                 VariantStockRepository variantStockRepository,
                                 StockMovementRepository stockMovementRepository,
                                 ProductVariantRepository productVariantRepository,
                                 LocationRepository locationRepository,
                                LowStockEventPublisher lowStockEventPublisher){
        this.stockTransferRepository = stockTransferRepository;
        this.variantStockRepository = variantStockRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.productVariantRepository = productVariantRepository;
        this.locationRepository = locationRepository;
        this.lowStockEventPublisher=lowStockEventPublisher;
    }

    @Transactional
    public StockTransferResponse transfer(StockTransferRequest request){
        if (request.getFromLocationId().equals(request.getToLocationId())){
            throw new IllegalArgumentException("Source and destination locations must be different.");
        }

        ProductVariant productVariant = productVariantRepository.findById(request.getVariantId())
            .orElseThrow(() -> new ResourceNotFoundException("Product variant with this id does not exist."));

        Location fromLocation = locationRepository.findById(request.getFromLocationId())
            .orElseThrow(() -> new ResourceNotFoundException("Source location with this id does not exist."));

        Location toLocation = locationRepository.findById(request.getToLocationId())
            .orElseThrow(() -> new ResourceNotFoundException("Destination location with this id does not exist."));

        VariantStock sourceStock = variantStockRepository
            .findForUpdate(productVariant.getId(), fromLocation.getId())
            .orElseThrow(() -> new IllegalStateException(
                "No stock record exists for SKU " + productVariant.getSku() + " at " + fromLocation.getName()));

        // Checked against availableQuantity, not onHandQuantity — this is
        // what stops staff from accidentally transferring away stock
        // that's already reserved for a pending order at this location.
        
        if (sourceStock.getAvailableQuantity() < request.getQuantity()){
            throw new IllegalStateException(
                "Insufficient available stock to transfer (available: " + sourceStock.getAvailableQuantity()
                + ", requested: " + request.getQuantity() + ")");
        }

        StockTransfer transfer = new StockTransfer();
        transfer.setProductVariant(productVariant);
        transfer.setFromLocation(fromLocation);
        transfer.setToLocation(toLocation);
        transfer.setQuantity(request.getQuantity());
        StockTransfer savedTransfer = stockTransferRepository.save(transfer);

        sourceStock.setOnHandQuantity(sourceStock.getOnHandQuantity() - request.getQuantity());
        variantStockRepository.save(sourceStock);
        lowStockEventPublisher.checkAndPublish(sourceStock);
        
        StockMovement outMovement = new StockMovement();
        outMovement.setProductVariant(productVariant);
        outMovement.setLocation(fromLocation);
        outMovement.setQuantityChange(-request.getQuantity());
        outMovement.setReason(StockMovementReason.TRANSFER_OUT);
        outMovement.setReferenceId(savedTransfer.getId());
        stockMovementRepository.save(outMovement);

        VariantStock destinationStock = variantStockRepository
            .findForUpdate(productVariant.getId(), toLocation.getId())
            .orElseGet(() -> {
                VariantStock newStock = new VariantStock();
                newStock.setProductVariant(productVariant);
                newStock.setLocation(toLocation);
                return newStock;
            });

        destinationStock.setOnHandQuantity(destinationStock.getOnHandQuantity() + request.getQuantity());
        variantStockRepository.save(destinationStock);

        StockMovement inMovement = new StockMovement();
        inMovement.setProductVariant(productVariant);
        inMovement.setLocation(toLocation);
        inMovement.setQuantityChange(request.getQuantity());
        inMovement.setReason(StockMovementReason.TRANSFER_IN);
        inMovement.setReferenceId(savedTransfer.getId());
        stockMovementRepository.save(inMovement);

        return StockTransferResponse.fromEntity(savedTransfer);
    }
}