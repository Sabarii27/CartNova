package com.sabari.cartnova.service;

import com.sabari.cartnova.dto.OrderResponse;
import com.sabari.cartnova.dto.PlaceOrderRequest;
import com.sabari.cartnova.entity.*;
import com.sabari.cartnova.exception.BadRequestException;
import com.sabari.cartnova.exception.InsufficientStockException;
import com.sabari.cartnova.exception.ResourceNotFoundException;
import com.sabari.cartnova.exception.UnauthorizedException;
import com.sabari.cartnova.repository.CartRepository;
import com.sabari.cartnova.repository.OrderRepository;
import com.sabari.cartnova.repository.ProductRepository;
import com.sabari.cartnova.repository.UserRepository;
import com.sabari.cartnova.util.EntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    /**
     * Checkout. Everything happens in ONE transaction: if any step fails (for example a product runs
     * out of stock) the whole thing rolls back, so no half-created order and no lost stock.
     */
    @Transactional
    public OrderResponse placeOrder(String email, PlaceOrderRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadRequestException("Your cart is empty"));
        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Your cart is empty");
        }

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PLACED);
        order.setShippingAddress(request.shippingAddress().trim());

        BigDecimal total = BigDecimal.ZERO;

        // Lock products in id order so two concurrent checkouts cannot deadlock each other
        List<CartItem> items = cart.getItems().stream()
                .sorted(Comparator.comparing(i -> i.getProduct().getId()))
                .toList();

        for (CartItem cartItem : items) {
            // Re-read the product from the DB with a row lock: current price, current stock
            Product product = productRepository.findByIdForUpdate(cartItem.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("A product in your cart no longer exists"));

            if (!product.isActive()) {
                throw new BadRequestException(product.getName() + " is no longer available. Please remove it from your cart.");
            }
            if (product.getStock() < cartItem.getQuantity()) {
                throw new InsufficientStockException("Only " + product.getStock() + " unit(s) of "
                        + product.getName() + " are available");
            }

            product.setStock(product.getStock() - cartItem.getQuantity());

            BigDecimal unitPrice = product.getPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()))
                    .setScale(2, RoundingMode.HALF_UP);

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(unitPrice);
            orderItem.setSubtotal(subtotal);
            order.addItem(orderItem);

            total = total.add(subtotal);
        }

        order.setTotalAmount(total);
        Order saved = orderRepository.save(order);
        cart.getItems().clear();

        return EntityMapper.toOrderResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(String email) {
        return orderRepository.findByUserEmailOrderByCreatedAtDesc(email).stream()
                .map(EntityMapper::toOrderResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getMyOrder(String email, Long orderId) {
        return EntityMapper.toOrderResponse(findOwnedOrder(email, orderId));
    }

    @Transactional
    public OrderResponse cancelMyOrder(String email, Long orderId) {
        Order order = findOwnedOrder(email, orderId);
        if (!order.getStatus().isCancellable()) {
            throw new BadRequestException("A " + order.getStatus() + " order can no longer be cancelled");
        }
        cancelAndRestock(order);
        return EntityMapper.toOrderResponse(order);
    }

    // ---------- admin ----------

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(EntityMapper::toOrderResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long orderId) {
        return EntityMapper.toOrderResponse(findOrder(orderId));
    }

    @Transactional
    public OrderResponse updateStatus(Long orderId, OrderStatus newStatus) {
        Order order = findOrder(orderId);
        OrderStatus current = order.getStatus();

        if (current == newStatus) {
            throw new BadRequestException("The order is already " + current);
        }
        if (!current.canTransitionTo(newStatus)) {
            throw new BadRequestException("An order cannot move from " + current + " to " + newStatus);
        }

        if (newStatus == OrderStatus.CANCELLED) {
            cancelAndRestock(order);
        } else {
            order.setStatus(newStatus);
        }
        return EntityMapper.toOrderResponse(order);
    }

    // ---------- helpers ----------

    private void cancelAndRestock(Order order) {
        List<OrderItem> items = order.getItems().stream()
                .sorted(Comparator.comparing(i -> i.getProduct().getId()))
                .toList();
        for (OrderItem item : items) {
            Product product = productRepository.findByIdForUpdate(item.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
            product.setStock(product.getStock() + item.getQuantity());
        }
        order.setStatus(OrderStatus.CANCELLED);
    }

    /** Users only ever see their own orders; someone else's order id looks like "not found". */
    private Order findOwnedOrder(String email, Long orderId) {
        return orderRepository.findByIdAndUserEmail(orderId, email)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id " + orderId));
    }

    private Order findOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id " + orderId));
    }
}
