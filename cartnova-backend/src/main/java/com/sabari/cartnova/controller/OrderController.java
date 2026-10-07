package com.sabari.cartnova.controller;

import com.sabari.cartnova.dto.OrderResponse;
import com.sabari.cartnova.dto.PlaceOrderRequest;
import com.sabari.cartnova.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> place(Authentication auth, @Valid @RequestBody PlaceOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.placeOrder(auth.getName(), request));
    }

    @GetMapping
    public List<OrderResponse> myOrders(Authentication auth) {
        return orderService.getMyOrders(auth.getName());
    }

    @GetMapping("/{id}")
    public OrderResponse myOrder(Authentication auth, @PathVariable Long id) {
        return orderService.getMyOrder(auth.getName(), id);
    }

    @PutMapping("/{id}/cancel")
    public OrderResponse cancel(Authentication auth, @PathVariable Long id) {
        return orderService.cancelMyOrder(auth.getName(), id);
    }
}
