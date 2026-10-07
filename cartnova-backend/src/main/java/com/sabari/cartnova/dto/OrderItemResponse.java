package com.sabari.cartnova.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        Long productId,
        String productName,
        String imageUrl,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal) {
}
