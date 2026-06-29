package com.nikos.retail.inventory;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import com.nikos.retail.productvariant.ProductVariant;

@Entity
@Table(name = "incoming_stock")
public class IncomingStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    private int expectedQuantity;
    private Integer receivedQuantity;

    private LocalDate expectedDate;

    @Enumerated(EnumType.STRING)
    private IncomingStockStatus status = IncomingStockStatus.EXPECTED;

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

    public int getExpectedQuantity() {return expectedQuantity;}
    public void setExpectedQuantity(int expectedQuantity){this.expectedQuantity = expectedQuantity;}

    public LocalDate getExpectedDate() {return expectedDate;}
    public void setExpectedDate(LocalDate expectedDate){this.expectedDate = expectedDate;}

    public Integer getReceivedQuantity(){return receivedQuantity;}
    public void setReceivedQuantity(Integer receivedQuantity){this.receivedQuantity = receivedQuantity;}
    
    public IncomingStockStatus getStatus() {return status;}
    public void setStatus(IncomingStockStatus status) {this.status = status;}

    public OffsetDateTime getCreatedAt() {return createdAt;}
    
}
