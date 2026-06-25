package com.nikos.retail.inventory;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long>{
    Optional<Location> findByName(String name);
}
