package com.nikos.retail.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
public interface BackorderRepository extends JpaRepository<Backorder, Long>{

    List<Backorder> findByProductVariant_IdAndStatusOrderByCreatedAtAsc(Long productVariantId, BackorderStatus status);
    List<Backorder> findByOrder_IdAndStatus(Long orderId, BackorderStatus status);
    /**
     * Used by the shortfall check in section 6.1 of the design doc: compare
     * this against the quantity that just arrived to decide whether
     * fulfillment can happen automatically or needs manual review.
     */
    
    @Query("select coalesce(sum(b.quantity), 0) from Backorder b " +
            "where b.productVariant.id = :variantId and b.status = :status")
    int sumPendingQuantity(@Param("variantId") Long variantId, @Param("status") BackorderStatus status);
}

