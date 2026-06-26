package com.nikos.retail.inventory;

import java.time.Instant;
import java.time.OffsetDateTime;

import com.nikos.retail.productvariant.ProductVariant;

import jakarta.annotation.*;
import jakarta.persistence.*;


@Entity
@Table(name = "stock_movements")
public class StockMovement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;
    
        @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    private int quantityChange;

    @Enumerated(EnumType.STRING)
    private StockMovementReason reason;

    private Long referenceId;

    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate(){
        createdAt = OffsetDateTime.now();
    }

    public Long getId(){return id;}

    public ProductVariant getProductVariant(){return productVariant;}
    public void setProductVariant(ProductVariant productVariant){this.productVariant = productVariant;}

    public Location getLocation(){return location;}
    public void setLocation(Location location){this.location = location;}

    public int getQuantityChange(){return quantityChange;}
    public void setQuantityChange(int quantityChange){this.quantityChange = quantityChange;}

    public StockMovementReason getReason(){return reason;}
    public void setReason(StockMovementReason reason){this.reason = reason;}

    public Long getReferenceId(){return referenceId;}
    public void setReferenceId(Long referenceId){this.referenceId = referenceId;}

    public OffsetDateTime getCreatedAt(){return createdAt;}

}
