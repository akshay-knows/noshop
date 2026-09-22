package com.noshop.product_service.service;

import com.noshop.product_service.dto.request.CreateProductRequest;
import com.noshop.product_service.dto.response.ProductRecommendationResponse;
import com.noshop.product_service.dto.response.ProductResponse;
import com.noshop.product_service.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Defines Product Catalog application operations.
 */
public interface ProductService {

    ProductResponse createProduct(CreateProductRequest request);

    ProductResponse getProductById(Long id);

    ProductResponse updateProduct(Long id, CreateProductRequest request);

    void deleteProduct(Long id);

    Page<ProductResponse> getAllProducts(
            Long categoryId,
            Long subCategoryId,
            ProductStatus status,
            Pageable pageable
    );

    Page<ProductResponse> searchProducts(String query, Pageable pageable);

    ProductResponse updateProductStatus(Long id, ProductStatus status);

    List<ProductRecommendationResponse> getSubstitutes(Long id, int limit);
}