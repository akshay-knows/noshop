package com.noshop.product_service.service.impl;

import com.noshop.common.exception.ResourceNotFoundException;
import com.noshop.product_service.dto.request.ImageUploadRequest;
import com.noshop.product_service.dto.response.ImageUploadResponse;
import com.noshop.product_service.dto.response.ProductImageResponse;
import com.noshop.product_service.entity.Product;
import com.noshop.product_service.entity.ProductImage;
import com.noshop.product_service.enums.ImageSource;
import com.noshop.product_service.mapper.ProductMapper;
import com.noshop.product_service.repository.ProductImageRepository;
import com.noshop.product_service.repository.ProductRepository;
import com.noshop.product_service.service.ProductImageService;
import com.noshop.product_service.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Manages product image metadata and the S3 upload/confirmation lifecycle. */
@Service
@RequiredArgsConstructor
public class ProductImageServiceImpl implements ProductImageService {

    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final S3Service s3Service;
    private final CacheManager cacheManager;

    @Value("${product.image.default-url:https://placehold.co/600x600/png?text=NoShop}")
    private String defaultImageUrl;

    /** Lists product images ordered by their display position. */
    @Override
    @Transactional(readOnly = true)
    public List<ProductImageResponse> getImagesByProductId(Long productId) {
        requireProduct(productId);

        return productImageRepository.findByProductIdOrderByDisplayOrderAsc(productId)
                .stream()
                .map(productMapper::toImageResponse)
                .toList();
    }

    /** Returns the primary image or a safe placeholder while no image is configured. */
    @Override
    @Transactional(readOnly = true)
    public ProductImageResponse getPrimaryImage(Long productId) {
        requireProduct(productId);

        return productImageRepository.findByProductIdAndDisplayOrder(productId, 1)
                .map(productMapper::toImageResponse)
                .orElseGet(() -> ProductImageResponse.builder()
                        .imageUrl(defaultImageUrl)
                        .altText("No product image available")
                        .displayOrder(1)
                        .source("DEFAULT")
                        .productId(productId)
                        .build());
    }

    /** Creates a short-lived S3 presigned upload URL and returns the eventual delivery URL. */
    @Override
    @Transactional(readOnly = true)
    public ImageUploadResponse generateUploadUrl(
            Long productId,
            ImageUploadRequest request) {
        requireProduct(productId);
        validateFileExtension(request.getFileName(), request.getContentType());

        String extension = getFileExtension(request.getFileName());
        String storageKey = "products/" + productId + "/images/"
                + UUID.randomUUID() + extension;

        return ImageUploadResponse.builder()
                .uploadUrl(s3Service.generatePresignedUploadUrl(
                        storageKey, request.getContentType()))
                .storageKey(storageKey)
                .imageUrl(s3Service.buildCloudFrontUrl(storageKey))
                .build();
    }

    /** Confirms an uploaded S3 object and persists its catalog metadata. */
    @Override
    @Transactional
    public ProductImageResponse confirmUpload(
            Long productId,
            ImageUploadRequest request,
            String storageKey,
            Integer displayOrder) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + productId));

        if (displayOrder == null || displayOrder < 1) {
            throw new IllegalArgumentException(
                    "Display order must be greater than or equal to 1");
        }

        boolean primaryExists = productImageRepository
                .existsByProductIdAndDisplayOrder(productId, 1);

        if (!primaryExists && displayOrder != 1) {
            throw new IllegalArgumentException(
                    "The first product image must have display order 1");
        }

        if (productImageRepository.existsByProductIdAndDisplayOrder(
                productId, displayOrder)) {
            throw new IllegalArgumentException(
                    "Image display order already exists: " + displayOrder);
        }

        validateStorageKey(productId, storageKey);
        validateFileExtension(request.getFileName(), request.getContentType());

        if (!s3Service.objectExists(storageKey)) {
            throw new IllegalArgumentException(
                    "Uploaded image does not exist in S3");
        }

        ProductImage image = ProductImage.builder()
                .imageUrl(s3Service.buildCloudFrontUrl(storageKey))
                .storageKey(storageKey)
                .altText(request.getAltText())
                .displayOrder(displayOrder)
                .source(ImageSource.CATALOG)
                .product(product)
                .build();

        ProductImageResponse response = productMapper.toImageResponse(
                productImageRepository.save(image));
        evictProductCache(productId);
        return response;
    }

    /** Deletes image metadata and its backing S3 object, then repairs primary-image ordering. */
    @Override
    @Transactional
    public void deleteImage(Long imageId) {
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product image not found with id: " + imageId));

        Long productId = image.getProduct().getId();
        boolean deletingPrimary = image.getDisplayOrder() == 1;

        s3Service.deleteFile(image.getStorageKey());
        productImageRepository.delete(image);
        productImageRepository.flush();

        if (deletingPrimary) {
            productImageRepository.findByProductIdOrderByDisplayOrderAsc(productId)
                    .stream()
                    .findFirst()
                    .ifPresent(nextImage -> {
                        nextImage.setDisplayOrder(1);
                        productImageRepository.save(nextImage);
                    });
        }

        evictProductCache(productId);
    }

    /** Validates that a client can only confirm an image inside its product prefix. */
    private void validateStorageKey(Long productId, String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            throw new IllegalArgumentException("Storage key is required");
        }

        String expectedPrefix = "products/" + productId + "/images/";
        if (!storageKey.startsWith(expectedPrefix)
                || storageKey.contains("..")
                || storageKey.contains("\\")) {
            throw new IllegalArgumentException("Invalid storage key for product");
        }
    }

    /** Accepts only supported image content types with matching file extensions. */
    private void validateFileExtension(String fileName, String contentType) {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("File name is required");
        }
        if (contentType == null || contentType.isBlank()) {
            throw new IllegalArgumentException("Content type is required");
        }

        String lowerFileName = fileName.toLowerCase(Locale.ROOT);
        String lowerContentType = contentType.toLowerCase(Locale.ROOT);

        boolean valid = switch (lowerContentType) {
            case "image/jpeg", "image/jpg" ->
                    lowerFileName.endsWith(".jpg") || lowerFileName.endsWith(".jpeg");
            case "image/png" -> lowerFileName.endsWith(".png");
            case "image/webp" -> lowerFileName.endsWith(".webp");
            default -> false;
        };

        if (!valid) {
            throw new IllegalArgumentException(
                    "File extension does not match content type");
        }
    }

    /** Extracts and normalizes the file extension for the S3 storage key. */
    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1 || lastDot == fileName.length() - 1) {
            throw new IllegalArgumentException("File must have an extension");
        }
        return fileName.substring(lastDot).toLowerCase(Locale.ROOT);
    }

    /** Requires the product to exist before operating on its images. */
    private Product requireProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + productId));
    }

    /** Evicts cached product data after an image changes. */
    private void evictProductCache(Long productId) {
        Cache cache = cacheManager.getCache("products");
        if (cache != null) {
            cache.evict(productId);
        }
    }
}
