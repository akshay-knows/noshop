package com.noshop.product_service.service.impl;

import com.noshop.product_service.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {
    private final S3Client s3Client;
    @Value("${aws.s3.bucket}")
    private String bucketName;
    @Value("${aws.region}")
    private String region;

    @Override
    public String uploadFile(MultipartFile file,
                             Long productId) throws IOException {
        String filename = "products/" + productId + "/" + UUID.randomUUID() + "-" + file.getOriginalFilename();

        s3Client.putObject(
                PutObjectRequest.builder()
                                .bucket(bucketName)
                                .key(filename)
                                .contentType(file.getContentType())
                                .build(),
                RequestBody.fromInputStream(
                        file.getInputStream(),
                        file.getSize()
                )
        );
        return "https://%s.s3.%s.amazonaws.com/%s".formatted(
                bucketName,
                region,
                filename
        );
    }
    @Override
    public void deleteFile(String s3Key) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                                                 .bucket(bucketName)
                                                 .key(s3Key)
                                                 .build());
    }


}
