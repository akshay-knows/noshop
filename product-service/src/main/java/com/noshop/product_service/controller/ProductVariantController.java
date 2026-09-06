package com.noshop.product_service.controller;

import com.noshop.product_service.dto.request.CreateProductVariantRequest;
import com.noshop.product_service.dto.response.ProductVariantResponse;
import com.noshop.product_service.service.ProductVariantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/product/variants")
@RequiredArgsConstructor
public class ProductVariantController {

    private final ProductVariantService productVariantService;

    @PostMapping
    public ResponseEntity<ProductVariantResponse> createProductVariant(
            @Valid @RequestBody CreateProductVariantRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(productVariantService.createProductVariant(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductVariantResponse> getProductVariantById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                productVariantService.getProductVariantById(id)
        );
    }

    @GetMapping
    public ResponseEntity<List<ProductVariantResponse>> getAllProductVariants() {

        return ResponseEntity.ok(
                productVariantService.getAllProductVariants()
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductVariantResponse> updateProductVariant(
            @PathVariable Long id,
            @Valid @RequestBody CreateProductVariantRequest request) {

        return ResponseEntity.ok(
                productVariantService.updateProductVariant(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductVariant(
            @PathVariable Long id) {

        productVariantService.deleteProductVariant(id);
        return ResponseEntity.noContent().build();
    }
}