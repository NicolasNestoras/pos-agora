package com.nikos.retail.inventory;


public class VariantStockResponse {
    private Long id;
    private Long productVariantId;
    private Long locationId;
    private int onHandQuantity;
    private int reservedQuantity;

    public VariantStockResponse(){}

    public static VariantStockResponse fromEntity(VariantStock variantStock){
        VariantStockResponse dto = new VariantStockResponse();
        dto.id = variantStock.getId();
        dto.productVariantId = variantStock.getProductVariant().getId();
        dto.locationId = variantStock.getLocation().getId();
        dto.onHandQuantity = variantStock.getOnHandQuantity();
        dto.reservedQuantity  = variantStock.getReservedQuantity();
        return dto;
    }

    public Long getId(){return id;}
    public Long getProductVariantId(){return productVariantId;}
    public Long getLocationId(){return locationId;}
    public int getOnHandQuantity(){return onHandQuantity;}
    public int getReservedQuantity(){return reservedQuantity;}
}
