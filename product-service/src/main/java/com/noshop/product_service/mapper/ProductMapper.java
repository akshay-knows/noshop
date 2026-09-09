package com.noshop.product_service.mapper;

import com.noshop.product_service.dto.request.CreateProductRequest;
import com.noshop.product_service.dto.response.ProductImageResponse;
import com.noshop.product_service.dto.response.ProductResponse;
import com.noshop.product_service.dto.response.ProductVariantResponse;
import com.noshop.product_service.entity.Product;
import com.noshop.product_service.entity.ProductImage;
import com.noshop.product_service.entity.ProductVariant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
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

    @Mapping(target = "brandId", source = "brand.id")
    @Mapping(target = "brandName", source = "brand.name")
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "subCategoryId", source = "subCategory.id")
    @Mapping(target = "subCategoryName", source = "subCategory.name")
    ProductResponse toResponse(Product product);

    void updateEntity(
            CreateProductRequest request,
            @MappingTarget Product product
    );

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    ProductVariantResponse toVariantResponse(ProductVariant variant);

    @Mapping(target = "productId", source = "product.id")
    ProductImageResponse toImageResponse(ProductImage image);
}
