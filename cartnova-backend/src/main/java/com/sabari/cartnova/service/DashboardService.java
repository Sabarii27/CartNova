package com.sabari.cartnova.service;

import com.sabari.cartnova.dto.DashboardResponse;
import com.sabari.cartnova.entity.OrderStatus;
import com.sabari.cartnova.entity.UserRole;
import com.sabari.cartnova.repository.OrderRepository;
import com.sabari.cartnova.repository.ProductRepository;
import com.sabari.cartnova.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private static final int LOW_STOCK_LIMIT = 5;

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public DashboardResponse getStats() {
        return new DashboardResponse(
                orderRepository.sumTotalAmountExcluding(OrderStatus.CANCELLED),
                orderRepository.count(),
                orderRepository.countByStatus(OrderStatus.PLACED),
                productRepository.countByActiveTrue(),
                productRepository.countByActiveTrueAndStockLessThanEqual(LOW_STOCK_LIMIT),
                userRepository.countByRole(UserRole.USER));
    }
}
