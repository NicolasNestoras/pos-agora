package com.nikos.retail.inventory;

public class StockTransferResponse {
    private Long id;
    private Long productVariantId;
    private Long fromLocationId;
    private Long toLocationId;
    private int quantity;
    private StockTransferStatus status;

    public StockTransferResponse(){}

    public static StockTransferResponse fromEntity(StockTransfer transfer){
        StockTransferResponse dto = new StockTransferResponse();
        dto.id = transfer.getId();
        dto.productVariantId = transfer.getProductVariant().getId();
        dto.fromLocationId = transfer.getFromLocation().getId();
        dto.toLocationId = transfer.getToLocation().getId();
        dto.quantity = transfer.getQuantity();
        dto.status = transfer.getStatus();
        return dto;
    }

    public Long getId(){return id;}
    public Long getVariantId(){return productVariantId;}
    public Long getFromLocationId(){return fromLocationId;}
    public Long getToLocationId(){return toLocationId;}
    public int getQuantity(){return quantity;}
    public StockTransferStatus getStatus(){return status;}
}