package com.nikos.retail.inventory;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/inventory/transfers")
public class StockTransferController {

    private final StockTransferService stockTransferService;

    public StockTransferController(StockTransferService stockTransferService){
        this.stockTransferService = stockTransferService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StockTransferResponse create(@Valid @RequestBody StockTransferRequest request){
        return stockTransferService.transfer(request);
    }
}