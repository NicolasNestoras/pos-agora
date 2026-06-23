package com.nikos.retail.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class CustomerRequest {
    
    @NotBlank(message = "Name cannot be blank.")
    private String name;

    @Email(message = "Email address must be valid.")
    private String email;

    private String phone;

    private CustomerType customerType;

    public String getName(){return name;}
    public void setName(String name){this.name = name;}

    public String getEmail(){return email;}
    public void setEmail(String email){this.email = email;}

    public String getPhone(){return phone;}
    public void setPhone(String phone){this.phone = phone;}

    public CustomerType getCustomerType(){return customerType;}
    public void setCustomerType(CustomerType customerType){this.customerType = customerType;}

}
