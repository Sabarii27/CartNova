package com.sabari.cartnova.service;

import com.sabari.cartnova.dto.AuthResponse;
import com.sabari.cartnova.dto.LoginRequest;
import com.sabari.cartnova.dto.RegisterRequest;
import com.sabari.cartnova.entity.User;
import com.sabari.cartnova.entity.UserRole;
import com.sabari.cartnova.exception.DuplicateResourceException;
import com.sabari.cartnova.repository.UserRepository;
import com.sabari.cartnova.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @InjectMocks private AuthService authService;

    @Test
    void register_savesUserWithUserRoleAndHashedPassword() {
        when(userRepository.existsByEmail("sabari@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("HASHED");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        AuthResponse response = authService.register(new RegisterRequest("Sabari", "Sabari@Example.com", "secret123"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertEquals(UserRole.USER, saved.getRole());
        assertEquals("HASHED", saved.getPassword());
        assertEquals("sabari@example.com", saved.getEmail());

        assertEquals("jwt-token", response.token());
        assertEquals(UserRole.USER, response.role());
        assertEquals(1L, response.userId());
    }

    @Test
    void register_rejectsDuplicateEmail() {
        when(userRepository.existsByEmail("sabari@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> authService.register(new RegisterRequest("Sabari", "sabari@example.com", "secret123")));

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_returnsTokenForValidCredentials() {
        User user = new User();
        user.setId(5L);
        user.setName("Admin");
        user.setEmail("admin@cartnova.com");
        user.setRole(UserRole.ADMIN);
        when(userRepository.findByEmail("admin@cartnova.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("admin-token");

        AuthResponse response = authService.login(new LoginRequest("Admin@cartnova.com", "Admin@123"));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        assertEquals("admin-token", response.token());
        assertEquals(UserRole.ADMIN, response.role());
    }

    @Test
    void login_propagatesBadCredentials() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        assertThrows(BadCredentialsException.class,
                () -> authService.login(new LoginRequest("user@cartnova.com", "wrong")));

        verify(jwtService, never()).generateToken(any());
    }
}
