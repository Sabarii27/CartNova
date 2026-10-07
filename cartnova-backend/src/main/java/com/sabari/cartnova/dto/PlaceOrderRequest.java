package com.sabari.cartnova.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PlaceOrderRequest(
        @NotBlank(message = "Delivery address is required")
        @Size(min = 10, max = 500, message = "Delivery address must be between 10 and 500 characters")
        String shippingAddress) {
}
