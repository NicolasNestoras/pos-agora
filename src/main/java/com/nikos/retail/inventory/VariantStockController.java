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
@RequestMapping("/api/inventory/stock")
public class VariantStockController {

    private final VariantStockService variantStockService;

    public VariantStockController(VariantStockService variantStockService){
        this.variantStockService = variantStockService;
    }

    @GetMapping("/location/{locationId}")
    public List<VariantStockResponse> getByLocation(@PathVariable Long locationId){
        return variantStockService.getStockByLocation(locationId);
    }

    @GetMapping("/variant/{variantId}")
    public List<VariantStockResponse> getByVariant(@PathVariable Long variantId){
        return variantStockService.getStockByVariant(variantId);
    }

    @PostMapping("/adjust")
    public VariantStockResponse adjust(@Valid @RequestBody StockAdjustmentRequest request){
        return variantStockService.adjustStock(request);
    }
}