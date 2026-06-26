package com.nikos.retail.inventory;

import java.time.OffsetDateTime;

import com.nikos.retail.productvariant.ProductVariant;

import jakarta.persistence.*;

@Entity
@Table(name = "stock_transfers")
public class StockTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "from_location_id", nullable = false)
    private Location fromLocation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "to_location_id", nullable = false)
    private Location toLocation;

    private int quantity;

    @Enumerated(EnumType.STRING)
    private StockTransferStatus status = StockTransferStatus.PENDING;

    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate(){
        createdAt = OffsetDateTime.now();
    }

    public Long getId(){return id;}

    public ProductVariant getProductVariant(){return productVariant;}
    public void setProductVariant(ProductVariant productVariant){this.productVariant =productVariant;}

    public Location getFromLocation(){return fromLocation;}
    public void setFromLocation(Location fromLocation){this.fromLocation = fromLocation;}

    public Location getToLocation(){return toLocation;}
    public void setToLocation(Location toLocation){this.toLocation = toLocation;}

    public int getQuantity(){return quantity;}
    public void setQuantity(int quantity){this.quantity = quantity;}

    public StockTransferStatus getStatus(){return status;}
    public void setStatus(StockTransferStatus status){this.status = status;}

    public OffsetDateTime getCreatedAt(){return createdAt;}

}
