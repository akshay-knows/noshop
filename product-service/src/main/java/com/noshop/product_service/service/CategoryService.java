package com.noshop.product_service.service;

import com.noshop.product_service.dto.request.CreateCategoryRequest;
import com.noshop.product_service.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {

    CategoryResponse createCategory(CreateCategoryRequest request);

    CategoryResponse getCategoryById(Long id);

    List<CategoryResponse> getAllCategories();

    CategoryResponse updateCategory(Long id, CreateCategoryRequest request);

    void deleteCategory(Long id);
}
