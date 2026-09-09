package com.noshop.product_service.service;

import com.noshop.product_service.dto.request.CreateProductRequest;
import com.noshop.product_service.dto.response.ProductResponse;
import com.noshop.product_service.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Defines the application operations for the Product Catalog domain.
 *
 * <p>The service coordinates product lifecycle operations, filtering,
 * pagination, search, and product status changes.</p>
 */
public interface ProductService {

    /** Creates a new product from the supplied request. */
    ProductResponse createProduct(CreateProductRequest request);

    /** Returns a product by its identifier. */
    ProductResponse getProductById(Long id);

    /** Updates an existing product. */
    ProductResponse updateProduct(Long id, CreateProductRequest request);

    /** Deletes a product by its identifier. */
    void deleteProduct(Long id);

    /** Returns a paginated product list with optional category and status filters. */
    Page<ProductResponse> getAllProducts(
            Long categoryId,
            Long subCategoryId,
            ProductStatus status,
            Pageable pageable
    );

    /** Searches products using the supplied search query. */
    Page<ProductResponse> searchProducts(String query, Pageable pageable);

    /** Changes the lifecycle status of an existing product. */
    ProductResponse updateProductStatus(Long id, ProductStatus status);
}
