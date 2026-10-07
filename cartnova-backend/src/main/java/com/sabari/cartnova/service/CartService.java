package com.sabari.cartnova.service;

import com.sabari.cartnova.dto.CartItemRequest;
import com.sabari.cartnova.dto.CartResponse;
import com.sabari.cartnova.entity.Cart;
import com.sabari.cartnova.entity.CartItem;
import com.sabari.cartnova.entity.Product;
import com.sabari.cartnova.entity.User;
import com.sabari.cartnova.exception.InsufficientStockException;
import com.sabari.cartnova.exception.ResourceNotFoundException;
import com.sabari.cartnova.exception.UnauthorizedException;
import com.sabari.cartnova.repository.CartRepository;
import com.sabari.cartnova.repository.ProductRepository;
import com.sabari.cartnova.repository.UserRepository;
import com.sabari.cartnova.util.EntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** The backend owns prices and totals. Only product ids and quantities come from the client. */
@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public CartResponse getCart(String email) {
        return EntityMapper.toCartResponse(getOrCreateCart(email));
    }

    public CartResponse addItem(String email, CartItemRequest request) {
        Cart cart = getOrCreateCart(email);
        Product product = productRepository.findByIdAndActiveTrue(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found or no longer available"));

        if (product.getStock() <= 0) {
            throw new InsufficientStockException(product.getName() + " is out of stock");
        }

        CartItem existing = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElse(null);

        int newQuantity = (existing == null ? 0 : existing.getQuantity()) + request.quantity();
        ensureStock(product, newQuantity);

        if (existing == null) {
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(newQuantity);
            cart.getItems().add(item);
        } else {
            existing.setQuantity(newQuantity);
        }

        cartRepository.flush();
        return EntityMapper.toCartResponse(cart);
    }

    public CartResponse updateItem(String email, Long cartItemId, int quantity) {
        Cart cart = getOrCreateCart(email);
        CartItem item = findItem(cart, cartItemId);
        ensureStock(item.getProduct(), quantity);
        item.setQuantity(quantity);

        cartRepository.flush();
        return EntityMapper.toCartResponse(cart);
    }

    public CartResponse removeItem(String email, Long cartItemId) {
        Cart cart = getOrCreateCart(email);
        CartItem item = findItem(cart, cartItemId);
        cart.getItems().remove(item); // orphanRemoval deletes the row

        cartRepository.flush();
        return EntityMapper.toCartResponse(cart);
    }

    public void clear(String email) {
        Cart cart = getOrCreateCart(email);
        cart.getItems().clear();
        cartRepository.flush();
    }

    private Cart getOrCreateCart(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        return cartRepository.findByUserId(user.getId()).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setUser(user);
            return cartRepository.save(cart);
        });
    }

    /** Looks only inside the caller's own cart, so one user can never touch another user's items. */
    private CartItem findItem(Cart cart, Long cartItemId) {
        return cart.getItems().stream()
                .filter(i -> i.getId() != null && i.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
    }

    private void ensureStock(Product product, int requested) {
        if (requested > product.getStock()) {
            throw new InsufficientStockException(
                    "Only " + product.getStock() + " unit(s) of " + product.getName() + " are available");
        }
    }
}
