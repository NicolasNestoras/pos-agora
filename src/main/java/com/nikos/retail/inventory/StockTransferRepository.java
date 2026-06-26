package com.nikos.retail.inventory;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StockTransferRepository extends JpaRepository<StockTransfer, Long>{
    
}
