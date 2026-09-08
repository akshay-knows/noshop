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

@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

    private final S3Client s3Client;

    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.cloudfront.domain}")
    private String cloudFrontDomain;


    @Override
    public String generatePresignedUploadUrl(
            String storageKey,
            String contentType) {

        PutObjectRequest putObjectRequest =
                PutObjectRequest.builder()
                                .bucket(bucketName)
                                .key(storageKey)
                                .contentType(contentType)
                                .build();

        PutObjectPresignRequest presignRequest =
                PutObjectPresignRequest.builder()
                                       .signatureDuration(
                                               Duration.ofMinutes(10)
                                       )
                                       .putObjectRequest(
                                               putObjectRequest
                                       )
                                       .build();

        return s3Presigner
                .presignPutObject(presignRequest)
                .url()
                .toString();
    }


    @Override
    public String buildCloudFrontUrl(
            String storageKey) {

        return "https://%s/%s"
                .formatted(
                        cloudFrontDomain,
                        storageKey
                );
    }


    @Override
    public void deleteFile(
            String storageKey) {

        s3Client.deleteObject(
                DeleteObjectRequest.builder()
                                   .bucket(bucketName)
                                   .key(storageKey)
                                   .build()
        );
    }


    @Override
    public boolean objectExists(
            String storageKey) {

        try {

            s3Client.headObject(
                    HeadObjectRequest.builder()
                                     .bucket(bucketName)
                                     .key(storageKey)
                                     .build()
            );

            return true;

        } catch (S3Exception e) {

            if (e.statusCode() == 404) {
                return false;
            }

            throw e;
        }
    }
}