package com.nikos.retail.inventory;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long>{
    Optional<Location> findByName(String name);
    List<Location> findByType(LocationType type);
}
