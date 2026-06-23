package com.nikos.retail.product;

import jakarta.validation.constraints.NotBlank;

public class ProductRequest {
    
    @NotBlank(message ="Name cannot be blank.")
    private String name;

    @NotBlank(message = "Category cannot be blank.")
    private String category;

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getCategory(){
        return category;
    }

    public void setCategory(String category){
        this.category = category;
    }
}
