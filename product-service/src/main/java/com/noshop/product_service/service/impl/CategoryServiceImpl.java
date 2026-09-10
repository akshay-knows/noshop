package com.noshop.product_service.service.impl;

import com.noshop.common.exception.DuplicateResourceException;
import com.noshop.common.exception.ResourceNotFoundException;
import com.noshop.product_service.dto.request.CreateCategoryRequest;
import com.noshop.product_service.dto.response.CategoryResponse;
import com.noshop.product_service.entity.Category;
import com.noshop.product_service.mapper.CategoryMapper;
import com.noshop.product_service.repository.CategoryRepository;
import com.noshop.product_service.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Manages product categories and their catalog lifecycle. */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    /** Creates a category after enforcing name and slug uniqueness. */
    @Override
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Category already exists: " + request.getName());
        }
        if (categoryRepository.existsBySlug(request.getSlug())) {
            throw new DuplicateResourceException("Category slug already exists: " + request.getSlug());
        }

        Category category = categoryMapper.toEntity(request);
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    /** Returns a category by identifier. */
    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + id));
        return categoryMapper.toResponse(category);
    }

    /** Returns all categories in the catalog. */
    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    /** Updates category details while preserving uniqueness constraints. */
    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CreateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + id));

        if (!category.getName().equals(request.getName())
                && categoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException(
                    "Category already exists with name: " + request.getName());
        }
        if (!category.getSlug().equals(request.getSlug())
                && categoryRepository.existsBySlug(request.getSlug())) {
            throw new DuplicateResourceException(
                    "Category slug already exists: " + request.getSlug());
        }

        category.setName(request.getName());
        category.setSlug(request.getSlug());
        category.setDisplayOrder(request.getDisplayOrder());

        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    /** Soft-deletes a category by marking it inactive. */
    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + id));

        category.setActive(false);
        categoryRepository.save(category);
    }
}
