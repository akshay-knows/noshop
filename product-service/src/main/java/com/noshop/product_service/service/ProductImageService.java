package com.noshop.product_service.service;

import com.noshop.product_service.dto.request.ImageUploadRequest;
import com.noshop.product_service.dto.response.ImageUploadResponse;
import com.noshop.product_service.dto.response.ProductImageResponse;

import java.util.List;

public interface ProductImageService {

    List<ProductImageResponse> getImagesByProductId(Long productId);

    ProductImageResponse getPrimaryImage(Long productId);

    ImageUploadResponse generateUploadUrl(
            Long productId,
            ImageUploadRequest request
    );

    ProductImageResponse confirmUpload(
            Long productId,
            ImageUploadRequest request,
            String storageKey,
            Integer displayOrder
    );

    void deleteImage(Long imageId);
}