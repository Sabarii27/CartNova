package com.sabari.cartnova.service;

import com.sabari.cartnova.dto.UserResponse;
import com.sabari.cartnova.exception.ResourceNotFoundException;
import com.sabari.cartnova.repository.UserRepository;
import com.sabari.cartnova.util.EntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserResponse getProfile(String email) {
        return userRepository.findByEmail(email)
                .map(EntityMapper::toUserResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .map(EntityMapper::toUserResponse)
                .toList();
    }

    public UserResponse getUser(Long id) {
        return userRepository.findById(id)
                .map(EntityMapper::toUserResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
    }
}
