package com.sabari.cartnova.service;

import com.sabari.cartnova.dto.ProductRequest;
import com.sabari.cartnova.dto.ProductResponse;
import com.sabari.cartnova.entity.Category;
import com.sabari.cartnova.entity.Product;
import com.sabari.cartnova.exception.BadRequestException;
import com.sabari.cartnova.exception.ResourceNotFoundException;
import com.sabari.cartnova.repository.CategoryRepository;
import com.sabari.cartnova.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;
    @InjectMocks private ProductService productService;

    private Category category;
    private Product product;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Electronics");

        product = new Product();
        product.setId(10L);
        product.setName("Laptop");
        product.setDescription("A very good laptop");
        product.setPrice(new BigDecimal("50000.00"));
        product.setStock(5);
        product.setCategory(category);
    }

    private ProductRequest request(Long categoryId) {
        return new ProductRequest("  Gaming Laptop ", "Powerful gaming laptop", new BigDecimal("75000.00"), 7, "", categoryId);
    }

    @Test
    void create_savesTrimmedProductInCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            p.setId(99L);
            return p;
        });

        ProductResponse response = productService.create(request(1L));

        assertEquals(99L, response.id());
        assertEquals("Gaming Laptop", response.name());
        assertEquals(new BigDecimal("75000.00"), response.price());
        assertEquals(7, response.stock());
        assertEquals("Electronics", response.categoryName());
        assertNull(response.imageUrl(), "blank image URL should be stored as null");
    }

    @Test
    void create_failsWhenCategoryMissing() {
        when(categoryRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.create(request(2L)));
        verify(productRepository, never()).save(any());
    }

    @Test
    void getById_returnsProduct() {
        when(productRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(product));

        ProductResponse response = productService.getById(10L);

        assertEquals("Laptop", response.name());
        assertEquals(1L, response.categoryId());
    }

    @Test
    void getById_throwsWhenMissingOrInactive() {
        when(productRepository.findByIdAndActiveTrue(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.getById(404L));
    }

    @Test
    void update_changesFields() {
        when(productRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductResponse response = productService.update(10L, request(1L));

        assertEquals("Gaming Laptop", response.name());
        assertEquals(new BigDecimal("75000.00"), response.price());
        assertEquals(7, response.stock());
    }

    @Test
    void delete_deactivatesProductInsteadOfRemovingRow() {
        when(productRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(product));

        productService.delete(10L);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        assertFalse(captor.getValue().isActive());
        verify(productRepository, never()).delete(any(Product.class));
    }

    @Test
    void search_rejectsMinPriceAboveMaxPrice() {
        assertThrows(BadRequestException.class, () -> productService.search(null, null,
                new BigDecimal("500"), new BigDecimal("100"), null, 0, 10, "price,asc"));
    }
}
