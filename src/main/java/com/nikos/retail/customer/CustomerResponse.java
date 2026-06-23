package com.nikos.retail.customer;

import java.time.OffsetDateTime;

public class CustomerResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private CustomerType type;
    private OffsetDateTime createdAt;
    

    public CustomerResponse(){}
    public static CustomerResponse fromEntity(Customer customer){
        
        CustomerResponse dto = new CustomerResponse();
        dto.id = customer.getId();
        dto.name = customer.getName();
        dto.email = customer.getEmail();
        dto.phone = customer.getPhone();
        dto.type = customer.getCustomerType();
        dto.createdAt = customer.getCreatedAt();
        return dto;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public CustomerType getType(){
        return type;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    } 
}
