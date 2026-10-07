package com.sabari.cartnova.dto;

import java.math.BigDecimal;

public record DashboardResponse(
        BigDecimal totalRevenue,
        long totalOrders,
        long pendingOrders,
        long totalProducts,
        long lowStockProducts,
        long totalUsers) {
}
