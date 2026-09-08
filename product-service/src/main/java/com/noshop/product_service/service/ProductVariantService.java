package com.noshop.product_service.service;

import com.noshop.product_service.dto.request.CreateProductVariantRequest;
import com.noshop.product_service.dto.response.ProductVariantResponse;

import java.util.List;

public interface ProductVariantService {
    ProductVariantResponse createVariant(Long productId, CreateProductVariantRequest request);

    List<ProductVariantResponse> getVariantsByProductId(Long productId);

    ProductVariantResponse getVariantById(Long variantId);

    ProductVariantResponse updateVariant(Long variantId, CreateProductVariantRequest request);

    void deleteVariant(Long variantId);
}