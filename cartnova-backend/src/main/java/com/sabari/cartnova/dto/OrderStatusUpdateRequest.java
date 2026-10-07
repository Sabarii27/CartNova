package com.sabari.cartnova.dto;

import com.sabari.cartnova.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusUpdateRequest(@NotNull(message = "Status is required") OrderStatus status) {
}
