package com.sabari.cartnova.service;

import com.sabari.cartnova.dto.CartItemRequest;
import com.sabari.cartnova.dto.CartResponse;
import com.sabari.cartnova.entity.Cart;
import com.sabari.cartnova.entity.CartItem;
import com.sabari.cartnova.entity.Product;
import com.sabari.cartnova.entity.User;
import com.sabari.cartnova.exception.InsufficientStockException;
import com.sabari.cartnova.exception.ResourceNotFoundException;
import com.sabari.cartnova.repository.CartRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    private static final String EMAIL = "user@cartnova.com";

    @Mock private CartRepository cartRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProductRepository productRepository;
    @InjectMocks private CartService cartService;

    private Cart cart;
    private Product product;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(1L);
        user.setEmail(EMAIL);

        cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);

        product = new Product();
        product.setId(10L);
        product.setName("Headphones");
        product.setPrice(new BigDecimal("1500.00"));
        product.setStock(5);
        product.setActive(true);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
    }

    private void stubProduct() {
        when(productRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(product));
    }

    private CartItem existingItem(long id, int quantity) {
        CartItem item = new CartItem();
        item.setId(id);
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(quantity);
        cart.getItems().add(item);
        return item;
    }

    @Test
    void addItem_createsNewItemAndBackendCalculatesTotals() {
        stubProduct();

        CartResponse response = cartService.addItem(EMAIL, new CartItemRequest(10L, 2));

        assertEquals(1, response.items().size());
        assertEquals(2, response.totalItems());
        assertEquals(new BigDecimal("1500.00"), response.items().get(0).unitPrice());
        assertEquals(new BigDecimal("3000.00"), response.totalAmount());
    }

    @Test
    void addItem_mergesQuantityWhenProductAlreadyInCart() {
        stubProduct();
        existingItem(100L, 2);

        CartResponse response = cartService.addItem(EMAIL, new CartItemRequest(10L, 1));

        assertEquals(1, response.items().size(), "should update the existing line, not add a second one");
        assertEquals(3, response.items().get(0).quantity());
    }

    @Test
    void addItem_rejectsQuantityAboveStock() {
        stubProduct();
        existingItem(100L, 4);

        assertThrows(InsufficientStockException.class,
                () -> cartService.addItem(EMAIL, new CartItemRequest(10L, 2)));
        assertEquals(4, cart.getItems().get(0).getQuantity(), "cart must not change on failure");
    }

    @Test
    void addItem_rejectsOutOfStockProduct() {
        product.setStock(0);
        stubProduct();

        assertThrows(InsufficientStockException.class,
                () -> cartService.addItem(EMAIL, new CartItemRequest(10L, 1)));
    }

    @Test
    void addItem_rejectsUnknownOrInactiveProduct() {
        when(productRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> cartService.addItem(EMAIL, new CartItemRequest(10L, 1)));
    }

    @Test
    void updateItem_changesQuantity() {
        existingItem(100L, 1);

        CartResponse response = cartService.updateItem(EMAIL, 100L, 4);

        assertEquals(4, response.items().get(0).quantity());
        assertEquals(new BigDecimal("6000.00"), response.totalAmount());
    }

    @Test
    void updateItem_rejectsQuantityAboveStock() {
        existingItem(100L, 1);

        assertThrows(InsufficientStockException.class, () -> cartService.updateItem(EMAIL, 100L, 6));
    }

    @Test
    void updateItem_cannotTouchItemOutsideOwnCart() {
        existingItem(100L, 1);

        assertThrows(ResourceNotFoundException.class, () -> cartService.updateItem(EMAIL, 999L, 2));
    }

    @Test
    void removeItem_removesLine() {
        existingItem(100L, 2);

        CartResponse response = cartService.removeItem(EMAIL, 100L);

        assertTrue(response.items().isEmpty());
        assertEquals(0, response.totalItems());
        assertEquals(0, BigDecimal.ZERO.compareTo(response.totalAmount()));
    }
}
