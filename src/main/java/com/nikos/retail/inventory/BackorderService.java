package com.nikos.retail.inventory;

import com.nikos.retail.common.exception.ResourceNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BackorderService {

    private final BackorderRepository backorderRepository;
    private final StockAllocationService stockAllocationService;
    private final LocationRepository locationRepository;

    public BackorderService(BackorderRepository backorderRepository,
                             StockAllocationService stockAllocationService,
                             LocationRepository locationRepository) {
        this.backorderRepository = backorderRepository;
        this.stockAllocationService = stockAllocationService;
        this.locationRepository = locationRepository;
    }

    public List<BackorderResponse> getPendingBackorders(Long variantId) {
        return backorderRepository
            .findByProductVariant_IdAndStatusOrderByCreatedAtAsc(variantId, BackorderStatus.PENDING)
            .stream()
            .map(BackorderResponse::fromEntity)
            .collect(Collectors.toList());
    }

    @Transactional
    public BackorderResponse fulfill(Long backorderId, BackorderFulfillRequest request) {
        Backorder backorder = backorderRepository.findById(backorderId)
            .orElseThrow(() -> new ResourceNotFoundException("Backorder with this id does not exist."));

        if (backorder.getStatus() != BackorderStatus.PENDING) {
            throw new IllegalStateException("Only PENDING backorders can be fulfilled.");
        }

        // Backorders are always fulfilled against warehouse stock — same
        // rule as Order allocation (inventory-design.md section 3.)

        Location warehouse = locationRepository.findByType(LocationType.WAREHOUSE)
            .stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No warehouse location is configured"));

        stockAllocationService.fulfillBackorder(backorder, warehouse, request.getQuantity());

        return BackorderResponse.fromEntity(backorder);
    }
}