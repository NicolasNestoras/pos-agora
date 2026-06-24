package com.nikos.retail.inventory;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long>{
    List<InventoryMovement> findByProductVariantIdOrderByCreatedAtDesc(Long productVariantId);
}
