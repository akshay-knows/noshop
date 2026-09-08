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
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductImageServiceImpl implements ProductImageService {

    private final ProductImageRepository productImageRepository;

    private final ProductRepository productRepository;

    private final ProductMapper productMapper;

    private final S3Service s3Service;


    @Override
    public List<ProductImageResponse> getImagesByProductId(
            Long productId) {

        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException(
                    "Product not found with id: " + productId
            );
        }

        return productImageRepository
                .findByProductIdOrderByDisplayOrderAsc(productId)
                .stream()
                .map(productMapper::toImageResponse)
                .toList();
    }


    @Override
    public ProductImageResponse getPrimaryImage(
            Long productId) {

        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException(
                    "Product not found with id: " + productId
            );
        }

        ProductImage image = productImageRepository
                .findByProductIdAndDisplayOrder(
                        productId,
                        1
                )
                .orElse(null);

        if (image == null) {
            return null;
        }

        return productMapper.toImageResponse(image);
    }


    @Override
    public ImageUploadResponse generateUploadUrl(
            Long productId,
            ImageUploadRequest request) {

        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException(
                    "Product not found with id: " + productId
            );
        }

        String extension =
                getFileExtension(
                        request.getFileName()
                );

        validateFileExtension(
                request.getFileName(),
                request.getContentType()
        );

        String storageKey =
                "products/"
                        + productId
                        + "/images/"
                        + UUID.randomUUID()
                        + extension;

        String uploadUrl =
                s3Service.generatePresignedUploadUrl(
                        storageKey,
                        request.getContentType()
                );

        String imageUrl =
                s3Service.buildCloudFrontUrl(
                        storageKey
                );

        return ImageUploadResponse.builder()
                                  .uploadUrl(uploadUrl)
                                  .storageKey(storageKey)
                                  .imageUrl(imageUrl)
                                  .build();
    }


    @Override
    public ProductImageResponse confirmUpload(
            Long productId,
            ImageUploadRequest request,
            String storageKey,
            Integer displayOrder) {

        Product product = productRepository.findById(productId)
                                           .orElseThrow(() ->
                                                                new ResourceNotFoundException(
                                                                        "Product not found with id: "
                                                                                + productId
                                                                )
                                           );

        if (displayOrder == null || displayOrder < 1) {
            throw new IllegalArgumentException(
                    "Display order must be greater than or equal to 1"
            );
        }

        if (productImageRepository
                .existsByProductIdAndDisplayOrder(
                        productId,
                        displayOrder
                )) {

            throw new IllegalArgumentException(
                    "Image display order already exists: "
                            + displayOrder
            );
        }

        // Security validation:
        // Make sure the storage key belongs to this product.
        validateStorageKey(
                productId,
                storageKey
        );

        // Make sure the file extension matches
        // the declared content type.
        validateFileExtension(
                request.getFileName(),
                request.getContentType()
        );

        // Make sure the client actually uploaded
        // the object to S3 before saving metadata.
        if (!s3Service.objectExists(storageKey)) {
            throw new IllegalArgumentException(
                    "Uploaded image does not exist in S3"
            );
        }

        String imageUrl =
                s3Service.buildCloudFrontUrl(
                        storageKey
                );

        ProductImage image = ProductImage.builder()
                                         .imageUrl(imageUrl)
                                         .storageKey(storageKey)
                                         .altText(request.getAltText())
                                         .displayOrder(displayOrder)
                                         .source(ImageSource.CATALOG)
                                         .product(product)
                                         .build();

        ProductImage savedImage =
                productImageRepository.save(image);

        return productMapper.toImageResponse(
                savedImage
        );
    }


    @Override
    public void deleteImage(
            Long imageId) {

        ProductImage image = productImageRepository
                .findById(imageId)
                .orElseThrow(() ->
                                     new ResourceNotFoundException(
                                             "Product image not found with id: "
                                                     + imageId
                                     )
                );

        if (image.getStorageKey() != null) {

            s3Service.deleteFile(
                    image.getStorageKey()
            );
        }

        productImageRepository.delete(image);
    }


    private void validateStorageKey(
            Long productId,
            String storageKey) {

        if (storageKey == null ||
                storageKey.isBlank()) {

            throw new IllegalArgumentException(
                    "Storage key is required"
            );
        }

        String expectedPrefix =
                "products/"
                        + productId
                        + "/images/";

        if (!storageKey.startsWith(expectedPrefix)) {

            throw new IllegalArgumentException(
                    "Invalid storage key for product"
            );
        }
    }


    private void validateFileExtension(
            String fileName,
            String contentType) {

        if (fileName == null ||
                fileName.isBlank()) {

            throw new IllegalArgumentException(
                    "File name is required"
            );
        }

        if (contentType == null ||
                contentType.isBlank()) {

            throw new IllegalArgumentException(
                    "Content type is required"
            );
        }

        String lowerFileName =
                fileName.toLowerCase();

        boolean valid = switch (contentType.toLowerCase()) {

            case "image/jpeg",
                 "image/jpg" ->
                    lowerFileName.endsWith(".jpg")
                            || lowerFileName.endsWith(".jpeg");

            case "image/png" ->
                    lowerFileName.endsWith(".png");

            case "image/webp" ->
                    lowerFileName.endsWith(".webp");

            default -> false;
        };

        if (!valid) {

            throw new IllegalArgumentException(
                    "File extension does not match content type"
            );
        }
    }


    private String getFileExtension(
            String fileName) {

        int lastDot =
                fileName.lastIndexOf('.');

        if (lastDot == -1) {

            throw new IllegalArgumentException(
                    "File must have an extension"
            );
        }

        return fileName
                .substring(lastDot)
                .toLowerCase();
    }
}