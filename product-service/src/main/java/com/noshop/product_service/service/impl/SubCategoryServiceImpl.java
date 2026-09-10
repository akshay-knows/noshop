package com.noshop.product_service.service.impl;

import com.noshop.common.exception.DuplicateResourceException;
import com.noshop.common.exception.ResourceNotFoundException;
import com.noshop.product_service.dto.request.CreateSubCategoryRequest;
import com.noshop.product_service.dto.response.SubCategoryResponse;
import com.noshop.product_service.entity.Category;
import com.noshop.product_service.entity.SubCategory;
import com.noshop.product_service.mapper.SubCategoryMapper;
import com.noshop.product_service.repository.CategoryRepository;
import com.noshop.product_service.repository.SubCategoryRepository;
import com.noshop.product_service.service.SubCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Manages subcategories belonging to product categories. */
@Service
@RequiredArgsConstructor
public class SubCategoryServiceImpl implements SubCategoryService {

    private final SubCategoryRepository subCategoryRepository;
    private final CategoryRepository categoryRepository;
    private final SubCategoryMapper subCategoryMapper;

    /** Creates a subcategory under an existing category. */
    @Override
    @Transactional
    public SubCategoryResponse createSubCategory(CreateSubCategoryRequest request) {
        if (subCategoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("SubCategory already exists: " + request.getName());
        }
        if (subCategoryRepository.existsBySlug(request.getSlug())) {
            throw new DuplicateResourceException("SubCategory slug already exists: " + request.getSlug());
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + request.getCategoryId()));

        SubCategory subCategory = subCategoryMapper.toEntity(request);
        subCategory.setCategory(category);

        return subCategoryMapper.toResponse(subCategoryRepository.save(subCategory));
    }

    /** Returns a subcategory by identifier. */
    @Override
    @Transactional(readOnly = true)
    public SubCategoryResponse getSubCategoryById(Long id) {
        SubCategory subCategory = subCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "SubCategory not found with id: " + id));
        return subCategoryMapper.toResponse(subCategory);
    }

    /** Returns all catalog subcategories. */
    @Override
    @Transactional(readOnly = true)
    public List<SubCategoryResponse> getAllSubCategories() {
        return subCategoryRepository.findAll()
                .stream()
                .map(subCategoryMapper::toResponse)
                .toList();
    }

    /** Updates subcategory data and its parent category. */
    @Override
    @Transactional
    public SubCategoryResponse updateSubCategory(Long id, CreateSubCategoryRequest request) {
        SubCategory subCategory = subCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "SubCategory not found with id: " + id));

        if (!subCategory.getName().equals(request.getName())
                && subCategoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException(
                    "SubCategory already exists with name: " + request.getName());
        }
        if (!subCategory.getSlug().equals(request.getSlug())
                && subCategoryRepository.existsBySlug(request.getSlug())) {
            throw new DuplicateResourceException(
                    "SubCategory slug already exists: " + request.getSlug());
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + request.getCategoryId()));

        subCategory.setName(request.getName());
        subCategory.setSlug(request.getSlug());
        subCategory.setDisplayOrder(request.getDisplayOrder());
        subCategory.setCategory(category);

        return subCategoryMapper.toResponse(subCategoryRepository.save(subCategory));
    }

    /** Soft-deletes a subcategory by marking it inactive. */
    @Override
    @Transactional
    public void deleteSubCategory(Long id) {
        SubCategory subCategory = subCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "SubCategory not found with id: " + id));

        subCategory.setActive(false);
        subCategoryRepository.save(subCategory);
    }
}
