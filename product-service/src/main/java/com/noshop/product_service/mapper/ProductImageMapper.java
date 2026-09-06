//package com.noshop.product_service.mapper;
//
//import com.noshop.product_service.dto.response.ProductImageResponse;
//import com.noshop.product_service.entity.ProductImage;
//import org.mapstruct.Mapper;
//import org.mapstruct.Mapping;
//
///**
// * Maps ProductImage entities to their response DTO. productId/productName
// * are pulled off the nested Product association via dot-path expressions.
// */
//@Mapper(componentModel = "spring")
//public interface ProductImageMapper {
//
//    @Mapping(source = "product.id", target = "productId")
//    @Mapping(source = "product.name", target = "productName")
//    ProductImageResponse toResponse(ProductImage entity);
//}