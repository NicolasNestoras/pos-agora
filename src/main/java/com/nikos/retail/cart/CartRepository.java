package com.nikos.retail.cart;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart,Long>{
    List<Cart> findByStatus(CartStatus status);
}
