package com.sabari.cartnova.dto;

import com.sabari.cartnova.entity.UserRole;

import java.time.LocalDateTime;

/** Never contains the password. */
public record UserResponse(Long id, String name, String email, UserRole role, LocalDateTime createdAt) {
}
