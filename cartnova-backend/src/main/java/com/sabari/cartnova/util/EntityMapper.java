package com.sabari.cartnova.util;

import com.sabari.cartnova.dto.*;
import com.sabari.cartnova.entity.*;

import java.math.BigDecimal;
import java.util.List;

/** Converts entities to DTOs. Always call inside a transaction so lazy relations can load. */
public final class EntityMapper {

    private EntityMapper() {
    }

    public static UserResponse toUserResponse(User u) {
        return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getRole(), u.getCreatedAt());
    }

    public static CategoryResponse toCategoryResponse(Category c) {
        return new CategoryResponse(c.getId(), c.getName(), c.getDescription());
    }

    public static ProductResponse toProductResponse(Product p) {
        return new ProductResponse(p.getId(), p.getName(), p.getDescription(), p.getPrice(), p.getStock(),
                p.getImageUrl(), p.getCategory().getId(), p.getCategory().getName(), p.getCreatedAt());
    }

    public static CartResponse toCartResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream().map(i -> {
            Product p = i.getProduct();
            BigDecimal subtotal = p.getPrice().multiply(BigDecimal.valueOf(i.getQuantity()));
            return new CartItemResponse(i.getId(), p.getId(), p.getName(), p.getImageUrl(), p.getPrice(),
                    i.getQuantity(), subtotal, p.getStock());
        }).toList();

        int totalItems = items.stream().mapToInt(CartItemResponse::quantity).sum();
        BigDecimal total = items.stream().map(CartItemResponse::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartResponse(cart.getId(), items, totalItems, total);
    }

    public static OrderResponse toOrderResponse(Order o) {
        List<OrderItemResponse> items = o.getItems().stream()
                .map(i -> new OrderItemResponse(i.getId(), i.getProduct().getId(), i.getProduct().getName(),
                        i.getProduct().getImageUrl(), i.getQuantity(), i.getUnitPrice(), i.getSubtotal()))
                .toList();
        return new OrderResponse(o.getId(), o.getUser().getId(), o.getUser().getName(), o.getUser().getEmail(),
                o.getTotalAmount(), o.getStatus(), o.getShippingAddress(), o.getCreatedAt(), o.getUpdatedAt(), items);
    }
}
