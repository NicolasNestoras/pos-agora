package com.nikos.retail.inventory;

import java.time.OffsetDateTime;

public class InventoryMovementResponse {
    
    private Long id;
    private Long referenceId;
    private Long productVariantId;
    private String sku;
    private InventoryMovementType type;
    private int quantityChange;
    private String note;
    private OffsetDateTime createdAt;

    public static InventoryMovementResponse fromEntity(InventoryMovement inventoryMovement){
        InventoryMovementResponse dto = new InventoryMovementResponse();
        dto.id = inventoryMovement.getId();
        dto.referenceId = inventoryMovement.getReferenceId();
        dto.productVariantId = inventoryMovement.getProductVariant().getId();
        dto.sku = inventoryMovement.getProductVariant().getSku();
        dto.type = inventoryMovement.getType();
        dto.quantityChange = inventoryMovement.getQuantityChange();
        dto.note = inventoryMovement.getNote();
        dto.createdAt = inventoryMovement.getCreatedAt();
        return dto;
    }

    public Long getId(){return id;}
    public Long getReferenceId(){return referenceId;}
    public Long getProductVariantId(){return productVariantId;}
    public String getSku(){return sku;}
    public InventoryMovementType getType(){return type;}
    public int getQuantityChange(){return quantityChange;}
    public String getNote(){return note;}
    public OffsetDateTime getCreatedAt(){return createdAt;}

}
