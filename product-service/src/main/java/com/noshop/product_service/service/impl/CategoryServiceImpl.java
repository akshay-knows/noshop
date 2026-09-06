package com.noshop.product_service.service.impl;

import com.noshop.common.exception.ResourceNotFoundException;
import com.noshop.product_service.dto.request.CreateCategoryRequest;
import com.noshop.product_service.dto.response.CategoryResponse;
import com.noshop.product_service.entity.Category;
import com.noshop.product_service.mapper.CategoryMapper;
import com.noshop.product_service.repository.CategoryRepository;
import com.noshop.product_service.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryResponse createCategory(CreateCategoryRequest request) {

        if (categoryRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Category already exists.");
        }

        if (categoryRepository.existsBySlug(request.getSlug())) {
            throw new IllegalArgumentException("Category slug already exists.");
        }

        Category category = categoryMapper.toEntity(request);

        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    public CategoryResponse getCategoryById(Long id) {

        Category category = categoryRepository.findById(id)
                                              .orElseThrow(() -> new ResourceNotFoundException("Category not found."));

        return categoryMapper.toResponse(category);
    }

    @Override
    public List<CategoryResponse> getAllCategories() {

        return categoryRepository.findAll()
                                 .stream()
                                 .map(categoryMapper::toResponse)
                                 .toList();
    }

    @Override
    public CategoryResponse updateCategory(Long id,
                                           CreateCategoryRequest request) {

        Category category = categoryRepository.findById(id)
                                              .orElseThrow(() -> new ResourceNotFoundException("Category not found."));

        category.setName(request.getName());
        category.setSlug(request.getSlug());
        category.setDisplayOrder(request.getDisplayOrder());

        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    public void deleteCategory(Long id) {

        Category category = categoryRepository.findById(id)
                                              .orElseThrow(() -> new ResourceNotFoundException("Category not found."));

        categoryRepository.delete(category);
    }

}