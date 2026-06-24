package com.nikos.retail.inventory;

import java.time.OffsetDateTime;

import com.nikos.retail.productvariant.ProductVariant;

import jakarta.persistence.*;

@Entity
@Table(name = "inventory_movements")
public class InventoryMovement {
    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long id;

    //We set it to positive when adding stock, negative when reducing.
    @Column(name = "quantity_change", nullable = false)
    private int quantityChange;

    //We don't set a default to Sale or Order, because it is interchangeable, and nullable
    @Column(name = "reference_id")
    private Long referenceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false)
    private InventoryMovementType type;

    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable= false)
    private ProductVariant productVariant;

    @Column
    private String note;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    public InventoryMovement(){}

    @PrePersist
    protected void onCreate(){
        createdAt = OffsetDateTime.now();
    }


    public Long getId(){return id;}

    public Long getReferenceId(){return referenceId;}
    public void setReferenceId(Long referenceId){this.referenceId = referenceId;}

    public int getQuantityChange(){return quantityChange;}
    public void setQuantityChange(int quantityChange){ this.quantityChange = quantityChange;}

    public ProductVariant getProductVariant(){return productVariant;}
    public void setProductVariant(ProductVariant productVariant){this.productVariant = productVariant;}

    public InventoryMovementType getType(){return type;}
    public void setType(InventoryMovementType type){this.type = type;}

    public String getNote(){return note;}
    public void setNote(String note){this.note = note;}

    public OffsetDateTime getCreatedAt(){return createdAt;}
}
