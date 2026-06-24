package com.nikos.retail.inventory;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {
    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService){
        this.inventoryService = inventoryService;
    }


    @PostMapping("/variants/{variantId}/adjust")
    public InventoryMovementResponse adjust(@PathVariable Long variantId, @Valid @RequestBody AdjustmentRequest request){
        
        return inventoryService.adjustStock(variantId, request);
    }

    @GetMapping("/variants/{variantId}/history")
    public List<InventoryMovementResponse> getHistory(@PathVariable Long variantId){
        return inventoryService.getHistoryForVariant(variantId);
    }
}
