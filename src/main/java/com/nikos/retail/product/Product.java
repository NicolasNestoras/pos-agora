package com.nikos.retail.product;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import com.nikos.retail.productvariant.ProductVariant;

import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class Product{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    //The following causes N+1 problem, since @OneToMany defaults to LAZY. Fix later.
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariant> variants = new ArrayList<>(); 

    @PrePersist
    protected void onCreate(){
        createdAt = OffsetDateTime.now();
    }

    public Long getId() { return id;}

    public String getName() {return name;}

    public void setName(String name) {this.name = name;}

    public String getCategory() {return category;}

    public void setCategory(String category) {this.category = category;}

    public OffsetDateTime getCreatedAt() {return createdAt;}

    public List<ProductVariant> getVariants() {return variants;}

    public void setVariants(List<ProductVariant> variants) {this.variants = variants;}

}
