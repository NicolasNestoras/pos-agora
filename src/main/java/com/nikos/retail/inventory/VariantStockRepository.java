package com.nikos.retail.inventory;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface VariantStockRepository extends JpaRepository<VariantStock, Long> {
    Optional<VariantStock> findByProductVariant_IdAndLocation_Id(Long productVariantId, Long locationId);
    List<VariantStock> findByLocation_Id(Long locationId);
    List<VariantStock> findByProductVariant_Id(Long productVariantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select vs from VariantStock vs where vs.productVariant.id = :variantId and vs.location.id = :locationId")
    Optional<VariantStock> findForUpdate(@Param("variantId") Long variantId, @Param("locationId") Long locationId);
    
}
