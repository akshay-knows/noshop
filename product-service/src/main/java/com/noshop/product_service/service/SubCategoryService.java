package com.noshop.product_service.service;

import com.noshop.product_service.dto.request.CreateSubCategoryRequest;
import com.noshop.product_service.dto.response.SubCategoryResponse;

import java.util.List;

public interface SubCategoryService {

    SubCategoryResponse createSubCategory(CreateSubCategoryRequest request);

    SubCategoryResponse getSubCategoryById(Long id);

    List<SubCategoryResponse> getAllSubCategories();

    SubCategoryResponse updateSubCategory(Long id, CreateSubCategoryRequest request);

    void deleteSubCategory(Long id);

}