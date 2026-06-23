package com.nikos.retail.customer;

import java.time.OffsetDateTime;

import jakarta.persistence.*;


@Entity
@Table(name = "customers")
public class Customer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerType customerType;

    @Column(name = "created_at", updatable= false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate(){
        createdAt = OffsetDateTime.now();
        if (customerType == null) {
            customerType = CustomerType.RETAIL;
        }
    }

    public Long getId() {return id;}

    public String getName() {return name;}

    public void setName(String name) {this.name = name;}

    public String getEmail() {return email;}

    public void setEmail(String email) {this.email = email;}

    public String getPhone() {return phone;}

    public void setPhone(String phone) {this.phone = phone;}

    public CustomerType getCustomerType() {return customerType;}

    public void setCustomerType(CustomerType customerType) {this.customerType = customerType;}

    public OffsetDateTime getCreatedAt() {return createdAt;}
}
