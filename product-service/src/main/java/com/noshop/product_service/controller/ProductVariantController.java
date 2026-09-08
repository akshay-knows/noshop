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

    @PostMapping("/product/{productId}")
    public ResponseEntity<ProductVariantResponse> createVariant(
            @PathVariable Long productId,
            @Valid @RequestBody CreateProductVariantRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(productVariantService.createVariant(
                        productId,
                        request
                ));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ProductVariantResponse>> getVariantsByProductId(
            @PathVariable Long productId) {

        return ResponseEntity.ok(
                productVariantService.getVariantsByProductId(productId)
        );
    }

    @GetMapping("/{variantId}")
    public ResponseEntity<ProductVariantResponse> getVariantById(
            @PathVariable Long variantId) {

        return ResponseEntity.ok(
                productVariantService.getVariantById(variantId)
        );
    }

    @PutMapping("/{variantId}")
    public ResponseEntity<ProductVariantResponse> updateVariant(
            @PathVariable Long variantId,
            @Valid @RequestBody CreateProductVariantRequest request) {

        return ResponseEntity.ok(
                productVariantService.updateVariant(
                        variantId,
                        request
                )
        );
    }

    @DeleteMapping("/{variantId}")
    public ResponseEntity<Void> deleteVariant(
            @PathVariable Long variantId) {

        productVariantService.deleteVariant(variantId);

        return ResponseEntity.noContent().build();
    }
}