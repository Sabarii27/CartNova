package com.sabari.cartnova.service;

import com.sabari.cartnova.dto.PageResponse;
import com.sabari.cartnova.dto.ProductRequest;
import com.sabari.cartnova.dto.ProductResponse;
import com.sabari.cartnova.entity.Category;
import com.sabari.cartnova.entity.Product;
import com.sabari.cartnova.exception.BadRequestException;
import com.sabari.cartnova.exception.ResourceNotFoundException;
import com.sabari.cartnova.repository.CategoryRepository;
import com.sabari.cartnova.repository.ProductRepository;
import com.sabari.cartnova.repository.ProductSpecifications;
import com.sabari.cartnova.util.EntityMapper;
import com.sabari.cartnova.util.PageRequestFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> search(String search, String category, BigDecimal minPrice,
                                                BigDecimal maxPrice, Boolean available,
                                                int page, int size, String sort) {
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new BadRequestException("Minimum price cannot be greater than maximum price");
        }
        var spec = ProductSpecifications.filter(search, category, minPrice, maxPrice, available);
        var pageable = PageRequestFactory.create(page, size, sort);
        return PageResponse.from(productRepository.findAll(spec, pageable).map(EntityMapper::toProductResponse));
    }

    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        return EntityMapper.toProductResponse(findActive(id));
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Category category = findCategory(request.categoryId());
        Product product = new Product();
        apply(product, request, category);
        return EntityMapper.toProductResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findActive(id);
        apply(product, request, findCategory(request.categoryId()));
        return EntityMapper.toProductResponse(productRepository.save(product));
    }

    /** Soft delete: the row stays so existing orders can still show the product. */
    @Transactional
    public void delete(Long id) {
        Product product = findActive(id);
        product.setActive(false);
        productRepository.save(product);
    }

    private Product findActive(Long id) {
        return productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + id));
    }

    private Category findCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id " + id));
    }

    private void apply(Product product, ProductRequest request, Category category) {
        product.setName(request.name().trim());
        product.setDescription(request.description().trim());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setImageUrl(request.imageUrl() == null || request.imageUrl().isBlank() ? null : request.imageUrl().trim());
        product.setCategory(category);
    }
}
