package com.nikos.retail.inventory;

import jakarta.persistence.*;


@Entity
@Table(name = "locations")
public class Location {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column( nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    private LocationType type;
    
    public Long getId(){return id;}

    public String getName(){return name;}
    public void setName(String name){this.name = name;}

    public LocationType getType(){return type;}
    public void setType(LocationType type){this.type = type;}


}
