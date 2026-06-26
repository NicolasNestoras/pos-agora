package com.nikos.retail.inventory;

import com.nikos.retail.order.Order;
import com.nikos.retail.productvariant.ProductVariant;
import jakarta.persistence.*;

import java.time.Instant;
import java.time.OffsetDateTime;

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    private int quantity;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status = ReservationStatus.ACTIVE;

    private Instant expiresAt;

    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate(){
        createdAt = OffsetDateTime.now();
    }

    public Long getId() {return id;}

    public ProductVariant getProductVariant() {return productVariant;}
    public void setProductVariant(ProductVariant productVariant){this.productVariant = productVariant;}

    public Location getLocation() {return location;}
    public void setLocation(Location location){this.location = location;}

    public Order getOrder() {return order;}
    public void setOrder(Order order){this.order = order;}

    public int getQuantity() {return quantity;}
    public void setQuantity(int quantity){this.quantity = quantity;}

    public ReservationStatus getStatus() {return status;}
    public void setStatus(ReservationStatus status) {this.status = status;}

    public Instant getExpiresAt() {return expiresAt;}
    public void setExpiresAt(Instant expiresAt) {this.expiresAt = expiresAt;}

    public OffsetDateTime getCreatedAt() {return createdAt;}
    
}

