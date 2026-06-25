package com.nikos.retail.inventory;


import com.nikos.retail.productvariant.ProductVariant;

import jakarta.persistence.*;


@Entity
@Table(name = "variant_stock", uniqueConstraints = @UniqueConstraint(columnNames = {"product_variant_id", "location_id"}))
public class VariantStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Column(name = "on_hand_quantity")
    private int onHandQuantity;

    @Column(name = "reserved_quantity")
    private int reservedQuantity;

    //Available quantity gets computed each time- Not persisted
    public int getAvailableQuantity(){
        return onHandQuantity-reservedQuantity;
    }
    public Long getId(){return id;}

    public int getReservedQuantity(){return reservedQuantity;}
    public void setReservedQuantity(int reservedQuantity){this.reservedQuantity = reservedQuantity;}

    public int getOnHandQuantity(){return onHandQuantity;}
    public void setOnHandQuantity(int onHandQuantity){this.onHandQuantity = onHandQuantity;}
    
    public ProductVariant getProductVariant(){return productVariant;}
    public void setProductVariant(ProductVariant productVariant){this.productVariant = productVariant;}

    public Location getLocation(){return location;}
    public void setLocation(Location location){this.location = location;}

}

