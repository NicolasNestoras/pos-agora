package com.nikos.retail.inventory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class LocationRequest {
    @NotBlank(message = "Name cannot be blank.")
    private String name;

    @NotNull(message = "Location Type cannot be null.")
    private LocationType type;

    public String getName(){return name;}
    public void setName(String name){this.name = name;}

    public LocationType getType(){return type;}
    public void setType(LocationType type){this.type = type;}
}
