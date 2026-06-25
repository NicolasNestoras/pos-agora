package com.nikos.retail.inventory;

public class LocationResponse {
    private Long id;
    private String name;
    private LocationType type;

    public LocationResponse(){}
    public static LocationResponse fromEntity(Location location){
        LocationResponse dto =  new LocationResponse();
        dto.id = location.getId();
        dto.name = location.getName();
        dto.type = location.getType();
        return dto;
    }

    public Long getId(){return id;}
    public String getName(){return name;}
    public LocationType getType(){return type;}
    

}
