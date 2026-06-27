package com.nikos.retail.inventory;

import com.nikos.retail.common.exception.ResourceNotFoundException;
import com.nikos.retail.productvariant.ProductVariant;
import com.nikos.retail.productvariant.ProductVariantRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class IncomingStockService {

    private final IncomingStockRepository incomingStockRepository;
    private final VariantStockRepository variantStockRepository;
    private final StockMovementRepository stockMovementRepository;
    private final BackorderRepository backorderRepository;
    private final StockAllocationService stockAllocationService;
    private final ProductVariantRepository productVariantRepository;
    private final LocationRepository locationRepository;

    public IncomingStockService(IncomingStockRepository incomingStockRepository,
                                 VariantStockRepository variantStockRepository,
                                 StockMovementRepository stockMovementRepository,
                                 BackorderRepository backorderRepository,
                                 StockAllocationService stockAllocationService,
                                 ProductVariantRepository productVariantRepository,
                                 LocationRepository locationRepository) {
        this.incomingStockRepository = incomingStockRepository;
        this.variantStockRepository = variantStockRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.backorderRepository = backorderRepository;
        this.stockAllocationService = stockAllocationService;
        this.productVariantRepository = productVariantRepository;
        this.locationRepository = locationRepository;
    }

    @Transactional
    public IncomingStockResponse create(IncomingStockRequest request) {
        ProductVariant variant = productVariantRepository.findById(request.getVariantId())
            .orElseThrow(() -> new ResourceNotFoundException("Product variant with this id does not exist."));
        Location location = locationRepository.findById(request.getLocationId())
            .orElseThrow(() -> new ResourceNotFoundException("Location with this id does not exist."));

        IncomingStock incomingStock = new IncomingStock();
        incomingStock.setProductVariant(variant);
        incomingStock.setLocation(location);
        incomingStock.setExpectedQuantity(request.getExpectedQuantity());
        incomingStock.setExpectedDate(request.getExpectedDate());

        return IncomingStockResponse.fromEntity(incomingStockRepository.save(incomingStock));
    }

    /**
     * Marks a shipment as received: restocks onHandQuantity, logs a
     * RESTOCK movement, then checks whether the amount received covers
     * every PENDING backorder for this variant.
     *
     * - Fully covered -> auto-fulfilled, oldest first. No real decision
     *   to make, so no point making a human click through it.
     * - Short -> nothing auto-fulfilled. Backorders stay PENDING until
     *   staff reviews the shortfall and decides the split manually
     *   (see BackorderController / inventory-design.md section 6.1).
     */
    @Transactional
    public IncomingStockResponse receive(Long id) {
        IncomingStock incomingStock = incomingStockRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Incoming stock record with this id does not exist."));

        if (incomingStock.getStatus() == IncomingStockStatus.RECEIVED) {
            throw new IllegalStateException("This incoming stock has already been received.");
        }

        ProductVariant variant = incomingStock.getProductVariant();
        Location location = incomingStock.getLocation();
        int receivedQuantity = incomingStock.getExpectedQuantity();

        VariantStock stock = variantStockRepository
            .findForUpdate(variant.getId(), location.getId())
            .orElseGet(() -> {
                VariantStock newStock = new VariantStock();
                newStock.setProductVariant(variant);
                newStock.setLocation(location);
                return newStock;
            });

        stock.setOnHandQuantity(stock.getOnHandQuantity() + receivedQuantity);
        variantStockRepository.save(stock);

        StockMovement movement = new StockMovement();
        movement.setProductVariant(variant);
        movement.setLocation(location);
        movement.setQuantityChange(receivedQuantity);
        movement.setReason(StockMovementReason.RESTOCK);
        movement.setReferenceId(incomingStock.getId());
        stockMovementRepository.save(movement);

        incomingStock.setStatus(IncomingStockStatus.RECEIVED);
        IncomingStock savedIncomingStock = incomingStockRepository.save(incomingStock);

        int pendingDemand = backorderRepository.sumPendingQuantity(variant.getId(), BackorderStatus.PENDING);

        if (pendingDemand > 0 && receivedQuantity >= pendingDemand) {
            List<Backorder> pendingBackorders = backorderRepository
                .findByProductVariant_IdAndStatusOrderByCreatedAtAsc(variant.getId(), BackorderStatus.PENDING);

            for (Backorder backorder : pendingBackorders) {
                stockAllocationService.fulfillBackorder(backorder, location, backorder.getQuantity());
            }
        }
        // pendingDemand > receivedQuantity: a genuine shortfall — leave
        // everything PENDING for manual review, on purpose.

        return IncomingStockResponse.fromEntity(savedIncomingStock);
    }
}