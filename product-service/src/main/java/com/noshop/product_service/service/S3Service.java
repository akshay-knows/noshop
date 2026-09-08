package com.noshop.product_service.service;

public interface S3Service {

    String generatePresignedUploadUrl(
            String storageKey,
            String contentType
    );

    String buildCloudFrontUrl(
            String storageKey
    );

    void deleteFile(
            String storageKey
    );
}