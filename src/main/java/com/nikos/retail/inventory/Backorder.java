package com.nikos.retail.inventory;

import com.nikos.retail.order.Order;
import com.nikos.retail.productvariant.ProductVariant;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "backorders")
public class Backorder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    private int quantity;

    @Enumerated(EnumType.STRING)
    private BackorderStatus status = BackorderStatus.PENDING;

    // Nullable — a backorder can exist before any incoming shipment is confirmed
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linked_incoming_stock_id")
    private IncomingStock linkedIncomingStock;

    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate(){
        createdAt = OffsetDateTime.now();
    }
    
    public Long getId(){return id;}

    public ProductVariant getProductVariant(){return productVariant;}
    public void setProductVariant(ProductVariant productVariant){this.productVariant = productVariant;}

    public Order getOrder() {return order;}
    public void setOrder(Order order){this.order = order;}

    public int getQuantity() {return quantity;}
    public void setQuantity(int quantity) {this.quantity = quantity;}

    public BackorderStatus getStatus() {return status;}
    public void setStatus(BackorderStatus status) {this.status = status;}

    public IncomingStock getLinkedIncomingStock() {return linkedIncomingStock;}
    public void setLinkedIncomingStock(IncomingStock linkedIncomingStock) {this.linkedIncomingStock = linkedIncomingStock;}

    public OffsetDateTime getCreatedAt() {return createdAt;}

}
