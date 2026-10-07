package com.sabari.cartnova.dto;

import com.sabari.cartnova.entity.UserRole;

public record AuthResponse(String token, Long userId, String name, String email, UserRole role) {
}
