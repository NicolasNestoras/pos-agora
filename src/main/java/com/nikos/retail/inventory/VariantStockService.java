package com.nikos.retail.inventory;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nikos.retail.common.exception.ResourceNotFoundException;
import com.nikos.retail.productvariant.ProductVariant;
import com.nikos.retail.productvariant.ProductVariantRepository;

@Service
public class VariantStockService {

    // Only the following reasons are allowed through manual adjustment. SALE,
    // ORDER_FULFILLED, TRANSFER_IN, and TRANSFER_OUT must only ever be set
    // by the system itself (checkout, transfer flows) — never by a staff
    // member typing a number into a form
    private static final Set<StockMovementReason> MANUAL_REASONS =
            Set.of(StockMovementReason.RESTOCK, StockMovementReason.ADJUSTMENT, StockMovementReason.RETURN);

    private final VariantStockRepository variantStockRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ProductVariantRepository productVariantRepository;
    private final LocationRepository locationRepository;

    public VariantStockService(VariantStockRepository variantStockRepository,
                                StockMovementRepository stockMovementRepository,
                                ProductVariantRepository productVariantRepository,
                                LocationRepository locationRepository){
        this.variantStockRepository = variantStockRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.productVariantRepository = productVariantRepository;
        this.locationRepository = locationRepository;
    }

    public List<VariantStockResponse> getStockByLocation(Long locationId){
        List<VariantStock> stock = variantStockRepository.findByLocation_Id(locationId);
        return stock
            .stream()
            .map(VariantStockResponse::fromEntity)
            .collect(Collectors.toList());
    }

    public List<VariantStockResponse> getStockByVariant(Long variantId){
        List<VariantStock> stock = variantStockRepository.findByProductVariant_Id(variantId);
        return stock
            .stream()
            .map(VariantStockResponse::fromEntity)
            .collect(Collectors.toList());
    }


    @Transactional
    public VariantStockResponse adjustStock(StockAdjustmentRequest request){
        if (!MANUAL_REASONS.contains(request.getReason())){
            throw new IllegalArgumentException(
                "Reason must be one of " + MANUAL_REASONS);
        }

        ProductVariant variant = productVariantRepository.findById(request.getVariantId())
            .orElseThrow(() -> new ResourceNotFoundException("Product variant with this id does not exist."));

        Location location = locationRepository.findById(request.getLocationId())
            .orElseThrow(() -> new ResourceNotFoundException("Location with this id does not exist."));

        VariantStock stock = variantStockRepository
            .findByProductVariant_IdAndLocation_Id(request.getVariantId(), request.getLocationId())
            .orElseGet(() -> {
                VariantStock newStock = new VariantStock();
                newStock.setProductVariant(variant);
                newStock.setLocation(location);
                return newStock;
            });

        int newOnHand = stock.getOnHandQuantity() + request.getQuantityChange();
        if (newOnHand < 0){
            throw new IllegalStateException("This adjustment would make on-hand quantity negative.");
        }
        stock.setOnHandQuantity(newOnHand);
        VariantStock savedStock = variantStockRepository.save(stock);

        StockMovement movement = new StockMovement();
        movement.setProductVariant(variant);
        movement.setLocation(location);
        movement.setQuantityChange(request.getQuantityChange());
        movement.setReason(request.getReason());
        stockMovementRepository.save(movement);

        return VariantStockResponse.fromEntity(savedStock);
    }
}