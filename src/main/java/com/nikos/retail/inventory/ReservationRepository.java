package com.nikos.retail.inventory;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByOrder_Id(Long orderId);
    List<Reservation> findByOrder_IdAndStatus(Long orderId, ReservationStatus status);
    List<Reservation> findByProductVariant_IdAndStatus(Long productVariantId, ReservationStatus status);
}
