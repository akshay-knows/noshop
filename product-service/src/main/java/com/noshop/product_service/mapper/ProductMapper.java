package com.noshop.product_service.mapper;

import com.noshop.product_service.dto.request.CreateProductRequest;
import com.noshop.product_service.dto.response.ProductImageResponse;
import com.noshop.product_service.dto.response.ProductResponse;
import com.noshop.product_service.dto.response.ProductVariantResponse;
import com.noshop.product_service.entity.Product;
import com.noshop.product_service.entity.ProductImage;
import com.noshop.product_service.entity.ProductVariant;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(
        componentModel = "spring",
        uses = {
                BrandMapper.class,
                CategoryMapper.class
        }
)
public interface ProductMapper {


    Product toEntity(CreateProductRequest request);


    ProductResponse toResponse(Product product);


    void updateEntity(
            CreateProductRequest request,
            @MappingTarget Product product
    );


    ProductVariantResponse toVariantResponse(ProductVariant variant);


    ProductImageResponse toImageResponse(ProductImage image);


}