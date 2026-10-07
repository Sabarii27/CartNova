package com.sabari.cartnova.controller;

import com.sabari.cartnova.dto.UserResponse;
import com.sabari.cartnova.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    @GetMapping
    public UserResponse profile(Authentication authentication) {
        return userService.getProfile(authentication.getName());
    }
}
