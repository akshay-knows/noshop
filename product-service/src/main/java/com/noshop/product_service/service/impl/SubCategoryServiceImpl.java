package com.noshop.product_service.service.impl;

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

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubCategoryServiceImpl implements SubCategoryService {

    private final SubCategoryRepository subCategoryRepository;
    private final CategoryRepository categoryRepository;
    private final SubCategoryMapper subCategoryMapper;

    @Override
    public SubCategoryResponse createSubCategory(CreateSubCategoryRequest request) {

        if (subCategoryRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("SubCategory already exists.");
        }

        if (subCategoryRepository.existsBySlug(request.getSlug())) {
            throw new IllegalArgumentException("SubCategory slug already exists.");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + request.getCategoryId()
                ));

        SubCategory subCategory = subCategoryMapper.toEntity(request);
        subCategory.setCategory(category);

        return subCategoryMapper.toResponse(
                subCategoryRepository.save(subCategory)
        );
    }

    @Override
    public SubCategoryResponse getSubCategoryById(Long id) {

        SubCategory subCategory = subCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "SubCategory not found with id: " + id
                ));

        return subCategoryMapper.toResponse(subCategory);
    }

    @Override
    public List<SubCategoryResponse> getAllSubCategories() {
        return subCategoryRepository.findAll()
                .stream()
                .map(subCategoryMapper::toResponse)
                .toList();
    }

    @Override
    public SubCategoryResponse updateSubCategory(
            Long id,
            CreateSubCategoryRequest request) {

        SubCategory subCategory = subCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "SubCategory not found with id: " + id
                ));

        if (!subCategory.getName().equals(request.getName())
                && subCategoryRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException(
                    "SubCategory already exists with name: " + request.getName()
            );
        }

        if (!subCategory.getSlug().equals(request.getSlug())
                && subCategoryRepository.existsBySlug(request.getSlug())) {
            throw new IllegalArgumentException(
                    "SubCategory slug already exists: " + request.getSlug()
            );
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + request.getCategoryId()
                ));

        subCategory.setName(request.getName());
        subCategory.setSlug(request.getSlug());
        subCategory.setDisplayOrder(request.getDisplayOrder());
        subCategory.setCategory(category);

        return subCategoryMapper.toResponse(
                subCategoryRepository.save(subCategory)
        );
    }

    @Override
    public void deleteSubCategory(Long id) {

        SubCategory subCategory = subCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "SubCategory not found with id: " + id
                ));

        subCategoryRepository.delete(subCategory);
    }
}
