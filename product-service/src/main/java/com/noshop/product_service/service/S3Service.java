package com.noshop.product_service.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface S3Service {
    String uploadFile(MultipartFile file,Long productId) throws IOException;
    public void deleteFile(String s3Key);
}
