package com.sabari.cartnova.service;

import com.sabari.cartnova.dto.OrderResponse;
import com.sabari.cartnova.dto.PlaceOrderRequest;
import com.sabari.cartnova.entity.*;
import com.sabari.cartnova.exception.BadRequestException;
import com.sabari.cartnova.exception.InsufficientStockException;
import com.sabari.cartnova.exception.ResourceNotFoundException;
import com.sabari.cartnova.repository.CartRepository;
import com.sabari.cartnova.repository.OrderRepository;
import com.sabari.cartnova.repository.ProductRepository;
import com.sabari.cartnova.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    private static final String EMAIL = "user@cartnova.com";

    @Mock private OrderRepository orderRepository;
    @Mock private CartRepository cartRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProductRepository productRepository;
    @InjectMocks private OrderService orderService;

    private User user;
    private Cart cart;
    private Product laptop;
    private Product mouse;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Sabari");
        user.setEmail(EMAIL);

        cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);

        laptop = product(10L, "Laptop", "1000.00", 10);
        mouse = product(11L, "Mouse", "250.50", 5);
    }

    private Product product(long id, String name, String price, int stock) {
        Product p = new Product();
        p.setId(id);
        p.setName(name);
        p.setPrice(new BigDecimal(price));
        p.setStock(stock);
        p.setActive(true);
        return p;
    }

    private void addToCart(Product p, int qty) {
        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProduct(p);
        item.setQuantity(qty);
        cart.getItems().add(item);
    }

    private void stubCheckoutBasics() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
    }

    private Order order(OrderStatus status, Product p, int qty) {
        Order o = new Order();
        o.setId(7L);
        o.setUser(user);
        o.setStatus(status);
        o.setTotalAmount(p.getPrice().multiply(BigDecimal.valueOf(qty)));
        OrderItem item = new OrderItem();
        item.setProduct(p);
        item.setQuantity(qty);
        item.setUnitPrice(p.getPrice());
        item.setSubtotal(p.getPrice().multiply(BigDecimal.valueOf(qty)));
        o.addItem(item);
        return o;
    }

    @Test
    void placeOrder_calculatesTotalFromDatabasePrices_reducesStockAndClearsCart() {
        stubCheckoutBasics();
        addToCart(laptop, 2);
        addToCart(mouse, 1);
        when(productRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(laptop));
        when(productRepository.findByIdForUpdate(11L)).thenReturn(Optional.of(mouse));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(50L);
            return o;
        });

        OrderResponse response = orderService.placeOrder(EMAIL, new PlaceOrderRequest("12 Main Street, Chennai 600001"));

        assertEquals(new BigDecimal("2250.50"), response.totalAmount()); // 2 x 1000.00 + 1 x 250.50
        assertEquals(OrderStatus.PLACED, response.status());
        assertEquals(2, response.items().size());
        assertEquals(new BigDecimal("1000.00"), response.items().get(0).unitPrice());
        assertEquals(8, laptop.getStock());
        assertEquals(4, mouse.getStock());
        assertTrue(cart.getItems().isEmpty(), "cart must be cleared after checkout");
    }

    @Test
    void placeOrder_failsForEmptyCart() {
        stubCheckoutBasics();

        assertThrows(BadRequestException.class,
                () -> orderService.placeOrder(EMAIL, new PlaceOrderRequest("12 Main Street, Chennai")));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void placeOrder_failsWhenStockIsInsufficient() {
        stubCheckoutBasics();
        addToCart(mouse, 6);
        when(productRepository.findByIdForUpdate(11L)).thenReturn(Optional.of(mouse));

        assertThrows(InsufficientStockException.class,
                () -> orderService.placeOrder(EMAIL, new PlaceOrderRequest("12 Main Street, Chennai")));

        verify(orderRepository, never()).save(any());
        assertEquals(5, mouse.getStock(), "stock must not change when checkout fails");
        assertEquals(1, cart.getItems().size(), "cart must be kept when checkout fails");
    }

    @Test
    void placeOrder_failsWhenProductWasDeactivated() {
        stubCheckoutBasics();
        addToCart(mouse, 1);
        mouse.setActive(false);
        when(productRepository.findByIdForUpdate(11L)).thenReturn(Optional.of(mouse));

        assertThrows(BadRequestException.class,
                () -> orderService.placeOrder(EMAIL, new PlaceOrderRequest("12 Main Street, Chennai")));
    }

    @Test
    void cancel_placedOrderRestoresStock() {
        Order placed = order(OrderStatus.PLACED, laptop, 3);
        laptop.setStock(7);
        when(orderRepository.findByIdAndUserEmail(7L, EMAIL)).thenReturn(Optional.of(placed));
        when(productRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(laptop));

        OrderResponse response = orderService.cancelMyOrder(EMAIL, 7L);

        assertEquals(OrderStatus.CANCELLED, response.status());
        assertEquals(10, laptop.getStock());
    }

    @Test
    void cancel_deliveredOrderIsRejected() {
        Order delivered = order(OrderStatus.DELIVERED, laptop, 1);
        when(orderRepository.findByIdAndUserEmail(7L, EMAIL)).thenReturn(Optional.of(delivered));

        assertThrows(BadRequestException.class, () -> orderService.cancelMyOrder(EMAIL, 7L));
        assertEquals(OrderStatus.DELIVERED, delivered.getStatus());
        verify(productRepository, never()).findByIdForUpdate(any());
    }

    @Test
    void getMyOrder_otherUsersOrderLooksNotFound() {
        when(orderRepository.findByIdAndUserEmail(7L, EMAIL)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.getMyOrder(EMAIL, 7L));
    }

    @Test
    void adminUpdateStatus_allowsValidTransition() {
        Order placed = order(OrderStatus.PLACED, laptop, 1);
        when(orderRepository.findById(7L)).thenReturn(Optional.of(placed));

        OrderResponse response = orderService.updateStatus(7L, OrderStatus.CONFIRMED);

        assertEquals(OrderStatus.CONFIRMED, response.status());
    }

    @Test
    void adminUpdateStatus_rejectsInvalidTransition() {
        Order placed = order(OrderStatus.PLACED, laptop, 1);
        when(orderRepository.findById(7L)).thenReturn(Optional.of(placed));

        assertThrows(BadRequestException.class, () -> orderService.updateStatus(7L, OrderStatus.DELIVERED));
        assertEquals(OrderStatus.PLACED, placed.getStatus());
    }

    @Test
    void adminUpdateStatus_cancelRestoresStock() {
        Order confirmed = order(OrderStatus.CONFIRMED, laptop, 2);
        laptop.setStock(8);
        when(orderRepository.findById(7L)).thenReturn(Optional.of(confirmed));
        when(productRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(laptop));

        orderService.updateStatus(7L, OrderStatus.CANCELLED);

        assertEquals(10, laptop.getStock());
    }
}
