package com.noshop.product_service.service;

import com.noshop.product_service.dto.request.CreateProductVariantRequest;
import com.noshop.product_service.dto.response.ProductVariantResponse;

import java.util.List;

public interface ProductVariantService {

    ProductVariantResponse createProductVariant(CreateProductVariantRequest request);

    ProductVariantResponse getProductVariantById(Long id);

    List<ProductVariantResponse> getAllProductVariants();

    ProductVariantResponse updateProductVariant(Long id,
                                                CreateProductVariantRequest request);

    void deleteProductVariant(Long id);

}