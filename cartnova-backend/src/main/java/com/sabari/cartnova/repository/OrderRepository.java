package com.sabari.cartnova.repository;

import com.sabari.cartnova.entity.Order;
import com.sabari.cartnova.entity.OrderStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"items", "items.product", "user"})
    List<Order> findByUserEmailOrderByCreatedAtDesc(String email);

    @EntityGraph(attributePaths = {"items", "items.product", "user"})
    Optional<Order> findByIdAndUserEmail(Long id, String email);

    @EntityGraph(attributePaths = {"items", "items.product", "user"})
    List<Order> findAllByOrderByCreatedAtDesc();

    @Override
    @EntityGraph(attributePaths = {"items", "items.product", "user"})
    Optional<Order> findById(Long id);

    long countByStatus(OrderStatus status);

    @Query("select coalesce(sum(o.totalAmount), 0) from Order o where o.status <> :status")
    BigDecimal sumTotalAmountExcluding(@Param("status") OrderStatus status);
}
