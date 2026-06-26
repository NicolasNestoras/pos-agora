package com.nikos.retail.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface IncomingStockRepository extends JpaRepository<IncomingStock, Long>{
    List<IncomingStock> findByProductVariant_IdAndStatus(Long productVariantId, IncomingStockStatus status);

} 
