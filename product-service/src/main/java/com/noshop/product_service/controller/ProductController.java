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

@RestController
@RequestMapping("/api/v1/product/products")
@RequiredArgsConstructor
public class ProductController {


    private final ProductService productService;


    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody CreateProductRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(productService.createProduct(request));
    }


    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long subCategoryId,
            @RequestParam(required = false) ProductStatus status, Pageable pageable) {

        return ResponseEntity.ok(productService.getAllProducts(
                categoryId,
                subCategoryId,
                status,
                pageable
        ));
    }


    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {

        return ResponseEntity.ok(productService.getProductById(id));
    }


    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id,
                                                         @Valid @RequestBody CreateProductRequest request) {

        return ResponseEntity.ok(productService.updateProduct(
                id,
                request
        ));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {

        productService.deleteProduct(id);

        return ResponseEntity.noContent()
                             .build();
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponse>> searchProducts(@RequestParam String query,
                                                                Pageable pageable) {

        return ResponseEntity.ok(productService.searchProducts(
                query,
                pageable
        ));
    }
    @PatchMapping("/{id}/status")
    public ResponseEntity<ProductResponse> updateProductStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductStatusRequest request) {

        return ResponseEntity.ok(
                productService.updateProductStatus(
                        id,
                        request.getStatus()
                )
        );
    }
}