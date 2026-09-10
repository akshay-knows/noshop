package com.noshop.product_service.service.impl;

import com.noshop.product_service.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;

/** Provides S3 upload, existence, deletion, and image URL operations. */
@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.cloudfront.domain}")
    private String cloudFrontDomain;

    @Value("${product.image.default-url:https://placehold.co/600x600/png?text=NoShop}")
    private String defaultImageUrl;

    /** Creates a short-lived presigned PUT URL for a product image upload. */
    @Override
    public String generatePresignedUploadUrl(String storageKey, String contentType) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(storageKey)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(putObjectRequest)
                .build();

        return s3Presigner.presignPutObject(presignRequest).url().toString();
    }

    /** Converts an S3 object key into its CloudFront delivery URL or a local fallback. */
    @Override
    public String buildCloudFrontUrl(String storageKey) {
        if (cloudFrontDomain == null
                || cloudFrontDomain.isBlank()
                || cloudFrontDomain.startsWith("YOUR_")) {
            return defaultImageUrl;
        }

        String normalizedDomain = cloudFrontDomain
                .replaceFirst("^https?://", "")
                .replaceAll("/$", "");

        return "https://%s/%s".formatted(normalizedDomain, storageKey);
    }

    /** Deletes an image object from the configured S3 bucket. */
    @Override
    public void deleteFile(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            return;
        }

        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(storageKey)
                .build());
    }

    /** Checks whether an image object exists in S3 without downloading it. */
    @Override
    public boolean objectExists(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            return false;
        }

        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(storageKey)
                    .build());
            return true;
        } catch (S3Exception ex) {
            if (ex.statusCode() == 404) {
                return false;
            }
            throw ex;
        }
    }
}
