package com.noshop.product_service.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.noshop.common.exception.ResourceNotFoundException;
import com.noshop.product_service.dto.request.CreateProductRequest;
import com.noshop.product_service.dto.response.ProductResponse;
import com.noshop.product_service.entity.*;
import com.noshop.product_service.enums.ProductStatus;
import com.noshop.product_service.event.ProductCreatedEvent;
import com.noshop.product_service.event.ProductVariantEvent;
import com.noshop.product_service.mapper.ProductMapper;
import com.noshop.product_service.outbox.OutboxEvent;
import com.noshop.product_service.outbox.OutboxEventRepository;
import com.noshop.product_service.outbox.OutboxStatus;
import com.noshop.product_service.repository.*;
import com.noshop.product_service.service.ProductService;
import com.noshop.product_service.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductMapper productMapper;
    private final S3Service s3Service;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {

        if (productRepository.existsBySlug(request.getSlug())) {
            throw new IllegalArgumentException("Product slug already exists");
        }

        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Brand not found with id: " + request.getBrandId()
                ));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + request.getCategoryId()
                ));

        SubCategory subCategory = subCategoryRepository.findById(request.getSubCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "SubCategory not found with id: " + request.getSubCategoryId()
                ));

        Product product = productMapper.toEntity(request);
        product.setBrand(brand);
        product.setCategory(category);
        product.setSubCategory(subCategory);

        Product savedProduct = productRepository.save(product);

        ProductCreatedEvent event = ProductCreatedEvent.builder()
                .productId(savedProduct.getId())
                .name(savedProduct.getName())
                .status(savedProduct.getStatus().name())
                .variants(
                        savedProduct.getVariants()
                                .stream()
                                .map(variant -> ProductVariantEvent.builder()
                                        .variantId(variant.getId())
                                        .sku(variant.getSku())
                                        .packSize(variant.getPackSize())
                                        .unit(variant.getUnit())
                                        .price(variant.getPrice())
                                        .build())
                                .toList()
                )
                .build();

        try {
            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .eventType("PRODUCT_CREATED")
                    .aggregateId(savedProduct.getId())
                    .payload(objectMapper.writeValueAsString(event))
                    .status(OutboxStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build();

            outboxEventRepository.save(outboxEvent);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Failed to serialize product-created event",
                    e
            );
        }

        return productMapper.toResponse(savedProduct);
    }

    @Override
    public Page<ProductResponse> getAllProducts(
            Long categoryId,
            Long subCategoryId,
            ProductStatus status,
            Pageable pageable) {

        Page<Product> products;

        if (categoryId != null && status != null) {
            products = productRepository.findByCategoryIdAndStatus(categoryId, status, pageable);
        } else if (subCategoryId != null && status != null) {
            products = productRepository.findBySubCategoryIdAndStatus(subCategoryId, status, pageable);
        } else if (categoryId != null) {
            products = productRepository.findByCategoryId(categoryId, pageable);
        } else if (subCategoryId != null) {
            products = productRepository.findBySubCategoryId(subCategoryId, pageable);
        } else if (status != null) {
            products = productRepository.findByStatus(status, pageable);
        } else {
            products = productRepository.findAll(pageable);
        }

        return mapProductsWithImages(products);
    }

    @Cacheable(value = "products", key = "#id")
    @Override
    public ProductResponse getProductById(Long id) {

        Product product = productRepository.findByIdWithImages(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + id
                ));

        return productMapper.toResponse(product);
    }

    @CacheEvict(value = "products", key = "#id")
    @Override
    public ProductResponse updateProduct(Long id, CreateProductRequest request) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + id
                ));

        if (productRepository.existsBySlugAndIdNot(request.getSlug(), id)) {
            throw new IllegalArgumentException("Product slug already exists");
        }

        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Brand not found with id: " + request.getBrandId()
                ));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + request.getCategoryId()
                ));

        SubCategory subCategory = subCategoryRepository.findById(request.getSubCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "SubCategory not found with id: " + request.getSubCategoryId()
                ));

        productMapper.updateEntity(request, product);
        product.setBrand(brand);
        product.setCategory(category);
        product.setSubCategory(subCategory);

        Product updatedProduct = productRepository.save(product);
        return productMapper.toResponse(updatedProduct);
    }

    @CacheEvict(value = "products", key = "#id")
    @Override
    @Transactional
    public void deleteProduct(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + id
                ));

        for (ProductImage image : product.getImages()) {
            if (image.getStorageKey() != null && !image.getStorageKey().isBlank()) {
                s3Service.deleteFile(image.getStorageKey());
            }
        }

        productRepository.delete(product);
    }

    @Override
    public Page<ProductResponse> searchProducts(String query, Pageable pageable) {

        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Search query must not be blank");
        }

        Page<Product> products = productRepository.searchProducts(query.trim(), pageable);
        return mapProductsWithImages(products);
    }

    @CacheEvict(value = "products", key = "#id")
    @Override
    public ProductResponse updateProductStatus(Long id, ProductStatus status) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + id
                ));

        if (status == null) {
            throw new IllegalArgumentException("Product status is required");
        }

        if (product.getStatus() == ProductStatus.DISCONTINUED
                && status != ProductStatus.DISCONTINUED) {
            throw new IllegalStateException(
                    "Discontinued product cannot be reactivated"
            );
        }

        product.setStatus(status);
        Product savedProduct = productRepository.save(product);

        return productMapper.toResponse(savedProduct);
    }

    private Page<ProductResponse> mapProductsWithImages(Page<Product> products) {

        List<Long> productIds = products.getContent()
                .stream()
                .map(Product::getId)
                .toList();

        if (productIds.isEmpty()) {
            return products.map(productMapper::toResponse);
        }

        Map<Long, List<ProductImage>> imagesByProductId = productImageRepository
                .findByProductIdInOrderByDisplayOrderAsc(productIds)
                .stream()
                .collect(Collectors.groupingBy(image -> image.getProduct().getId()));

        return products.map(product -> {
            ProductResponse response = productMapper.toResponse(product);
            List<ProductImage> images = imagesByProductId.getOrDefault(
                    product.getId(),
                    Collections.emptyList()
            );

            response.setImages(
                    images.stream()
                            .map(productMapper::toImageResponse)
                            .toList()
            );

            return response;
        });
    }
}
