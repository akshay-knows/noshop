package com.noshop.product_service.mapper;

import com.noshop.product_service.dto.request.CreateCategoryRequest;
import com.noshop.product_service.dto.response.CategoryResponse;
import com.noshop.product_service.entity.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryResponse toResponse(Category category);
    Category toEntity (CreateCategoryRequest request);
}