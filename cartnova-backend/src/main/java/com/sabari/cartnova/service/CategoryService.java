package com.sabari.cartnova.service;

import com.sabari.cartnova.dto.CategoryRequest;
import com.sabari.cartnova.dto.CategoryResponse;
import com.sabari.cartnova.entity.Category;
import com.sabari.cartnova.exception.BadRequestException;
import com.sabari.cartnova.exception.DuplicateResourceException;
import com.sabari.cartnova.exception.ResourceNotFoundException;
import com.sabari.cartnova.repository.CategoryRepository;
import com.sabari.cartnova.repository.ProductRepository;
import com.sabari.cartnova.util.EntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll(Sort.by("name")).stream().map(EntityMapper::toCategoryResponse).toList();
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String name = request.name().trim();
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("A category named '" + name + "' already exists");
        }
        Category category = new Category();
        category.setName(name);
        category.setDescription(request.description());
        return EntityMapper.toCategoryResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id " + id));
        String name = request.name().trim();
        if (!category.getName().equalsIgnoreCase(name) && categoryRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("A category named '" + name + "' already exists");
        }
        category.setName(name);
        category.setDescription(request.description());
        return EntityMapper.toCategoryResponse(categoryRepository.save(category));
    }

    @Transactional
    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id " + id));
        if (productRepository.existsByCategoryId(id)) {
            throw new BadRequestException("This category still has products. Move or delete them first.");
        }
        categoryRepository.delete(category);
    }
}
