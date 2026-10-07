package com.sabari.cartnova.controller;

import com.sabari.cartnova.dto.CartItemRequest;
import com.sabari.cartnova.dto.CartQuantityRequest;
import com.sabari.cartnova.dto.CartResponse;
import com.sabari.cartnova.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public CartResponse getCart(Authentication auth) {
        return cartService.getCart(auth.getName());
    }

    @PostMapping("/items")
    public CartResponse addItem(Authentication auth, @Valid @RequestBody CartItemRequest request) {
        return cartService.addItem(auth.getName(), request);
    }

    @PutMapping("/items/{cartItemId}")
    public CartResponse updateItem(Authentication auth, @PathVariable Long cartItemId,
                                   @Valid @RequestBody CartQuantityRequest request) {
        return cartService.updateItem(auth.getName(), cartItemId, request.quantity());
    }

    @DeleteMapping("/items/{cartItemId}")
    public CartResponse removeItem(Authentication auth, @PathVariable Long cartItemId) {
        return cartService.removeItem(auth.getName(), cartItemId);
    }

    @DeleteMapping
    public ResponseEntity<Void> clear(Authentication auth) {
        cartService.clear(auth.getName());
        return ResponseEntity.noContent().build();
    }
}
