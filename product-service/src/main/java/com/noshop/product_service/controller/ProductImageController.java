package com.noshop.product_service.controller;

import com.noshop.common.response.ApiResponse;
import com.noshop.product_service.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/product/product-images")
@RequiredArgsConstructor
public class ProductImageController {

    private final S3Service s3Service;

    @PostMapping("/{productId}")
    public ResponseEntity<ApiResponse<String>> uploadProductImage(
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file) throws IOException {

        String imageUrl = s3Service.uploadFile(file, productId);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                           .success(true)
                           .message("Product image uploaded successfully.")
                           .data(imageUrl)
                           .build()
        );
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteProductImage(@RequestParam String s3Key) {
        s3Service.deleteFile(s3Key);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                           .success(true)
                           .message("Product image deleted successfully.")
                           .build()
        );
    }
}