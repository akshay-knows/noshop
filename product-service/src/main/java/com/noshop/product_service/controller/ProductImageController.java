package com.noshop.product_service.controller;

import com.noshop.product_service.dto.request.ImageUploadRequest;
import com.noshop.product_service.dto.response.ImageUploadResponse;
import com.noshop.product_service.dto.response.ProductImageResponse;
import com.noshop.product_service.service.ProductImageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/product/product-images")
@RequiredArgsConstructor
public class ProductImageController {

    private final ProductImageService productImageService;

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ProductImageResponse>> getProductImages(
            @PathVariable Long productId) {

        return ResponseEntity.ok(
                productImageService.getImagesByProductId(productId)
        );
    }

    @GetMapping("/product/{productId}/primary")
    public ResponseEntity<ProductImageResponse> getPrimaryImage(
            @PathVariable Long productId) {

        ProductImageResponse response =
                productImageService.getPrimaryImage(productId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/product/{productId}/upload-url")
    public ResponseEntity<ImageUploadResponse> generateUploadUrl(
            @PathVariable Long productId,
            @Valid @RequestBody ImageUploadRequest request) {

        return ResponseEntity.ok(
                productImageService.generateUploadUrl(
                        productId,
                        request
                )
        );
    }

    @PostMapping("/product/{productId}/confirm")
    public ResponseEntity<ProductImageResponse> confirmUpload(
            @PathVariable Long productId,
            @Valid @RequestBody ImageUploadRequest request,
            @RequestParam String storageKey,
            @RequestParam Integer displayOrder) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        productImageService.confirmUpload(
                                productId,
                                request,
                                storageKey,
                                displayOrder
                        )
                );
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> deleteImage(
            @PathVariable Long imageId) {

        productImageService.deleteImage(imageId);

        return ResponseEntity.noContent().build();
    }
}