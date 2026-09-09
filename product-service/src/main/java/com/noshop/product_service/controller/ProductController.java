package com.noshop.product_service.controller;

import com.noshop.product_service.dto.request.CreateProductRequest;
import com.noshop.product_service.dto.request.UpdateProductStatusRequest;
import com.noshop.product_service.dto.response.ProductResponse;
import com.noshop.product_service.enums.ProductStatus;
import com.noshop.product_service.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API for product catalog operations.
 *
 * <p>Supports product lifecycle management, pagination, filtering,
 * searching and status changes.</p>
 */
@RestController
@RequestMapping("/api/v1/product/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /** Creates a new catalog product. */
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.createProduct(request));
    }

    /** Returns a paginated product catalog with optional filters. */
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long subCategoryId,
            @RequestParam(required = false) ProductStatus status,
            Pageable pageable) {
        return ResponseEntity.ok(productService.getAllProducts(
                categoryId, subCategoryId, status, pageable));
    }

    /** Returns a product by identifier. */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    /** Updates an existing product. */
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    /** Deletes a product by identifier. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    /** Searches products using the supplied query text. */
    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponse>> searchProducts(
            @RequestParam String query,
            Pageable pageable) {
        return ResponseEntity.ok(productService.searchProducts(query, pageable));
    }

    /** Changes the lifecycle status of a product. */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ProductResponse> updateProductStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductStatusRequest request) {
        return ResponseEntity.ok(productService.updateProductStatus(
                id, request.getStatus()));
    }
}
