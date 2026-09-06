package com.noshop.product_service.mapper;

import com.noshop.product_service.dto.request.CreateProductVariantRequest;
import com.noshop.product_service.dto.response.ProductVariantResponse;
import com.noshop.product_service.entity.ProductVariant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductVariantMapper {

    ProductVariant toEntity(CreateProductVariantRequest request);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "sizeId", source = "size.id")
    @Mapping(target = "sizeName", source = "size.name")
    ProductVariantResponse toResponse(ProductVariant productVariant);

}