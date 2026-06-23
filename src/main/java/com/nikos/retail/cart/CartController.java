package com.nikos.retail.cart;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/carts")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // POST /api/carts                  -> anonymous cart (POS walk-in)
    // POST /api/carts?customerId=5     -> cart linked to a customer
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CartResponse create(@RequestParam(required = false) Long customerId) {
        return cartService.createCart(customerId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CartResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(cartService.getCartById(id));
    }

    @PostMapping("/{id}/items")
    public CartResponse addItem(@PathVariable Long id, @Valid @RequestBody CartItemRequest request) {
        return cartService.addItem(id, request);
    }

    @PostMapping("/{id}/hold")
    public CartResponse hold(@PathVariable Long id) {
        return cartService.holdCart(id);
    }

    @PostMapping("/{id}/resume")
    public CartResponse resume(@PathVariable Long id) {
        return cartService.resumeCart(id);
    }

    @GetMapping("/held")
    public List<CartResponse> getHeldCarts() {
        return cartService.getHeldCarts();
    }
}