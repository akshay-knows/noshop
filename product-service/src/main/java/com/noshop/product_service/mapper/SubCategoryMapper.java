package com.noshop.product_service.mapper;

import com.noshop.product_service.dto.request.CreateSubCategoryRequest;
import com.noshop.product_service.dto.response.SubCategoryResponse;
import com.noshop.product_service.entity.SubCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SubCategoryMapper {

    SubCategory toEntity(CreateSubCategoryRequest request);

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    SubCategoryResponse toResponse(SubCategory subCategory);

}