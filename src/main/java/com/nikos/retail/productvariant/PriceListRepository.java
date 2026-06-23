package com.nikos.retail.productvariant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PriceListRepository extends JpaRepository<PriceList, Long> {
    Optional<PriceList> findByName(String name);
}