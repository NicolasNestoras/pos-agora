package com.nikos.retail.inventory;

import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/inventory/backorders")
public class BackorderController {

    private final BackorderService backorderService;

    public BackorderController(BackorderService backorderService){
        this.backorderService = backorderService;
    }

    // The shortfall view: what's still pending for this product variant.

    @GetMapping("/variant/{variantId}/pending")
    public List<BackorderResponse> getPending(@PathVariable Long variantId){
        return backorderService.getPendingBackorders(variantId);
    }

    @PostMapping("/{id}/fulfill")
    public BackorderResponse fulfill(@PathVariable Long id, @Valid @RequestBody BackorderFulfillRequest request){
        return backorderService.fulfill(id, request);
    }
}
