package com.sabari.cartnova.controller;

import com.sabari.cartnova.dto.DashboardResponse;
import com.sabari.cartnova.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public DashboardResponse stats() {
        return dashboardService.getStats();
    }
}
