package com.noshop.product_service.mapper;

import com.noshop.product_service.dto.response.BrandResponse;
import com.noshop.product_service.entity.Brand;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BrandMapper {
    BrandResponse toResponse(Brand brand);
}
