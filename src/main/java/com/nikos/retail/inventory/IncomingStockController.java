package com.nikos.retail.inventory;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/inventory/incoming-stock")
public class IncomingStockController {

    private final IncomingStockService incomingStockService;

    public IncomingStockController(IncomingStockService incomingStockService){
        this.incomingStockService = incomingStockService;
    }

    @GetMapping
    public List<IncomingStockResponse> getAll(){
        return incomingStockService.getIncomingStock();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IncomingStockResponse create(@Valid @RequestBody IncomingStockRequest request){
        return incomingStockService.create(request);
    }

    @PostMapping("/{id}/receive")
    public IncomingStockResponse receive(@PathVariable Long id, @Valid @RequestBody ReceiveIncomingStockRequest request ){
        return incomingStockService.receive(id, request);
    }

    
}