package com.noshop.product_service.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.noshop.common.exception.DuplicateResourceException;
import com.noshop.common.exception.OperationNotAllowedException;
import com.noshop.common.exception.ResourceNotFoundException;
import com.noshop.product_service.dto.request.CreateProductRequest;
import com.noshop.product_service.dto.response.ProductResponse;
import com.noshop.product_service.entity.Brand;
import com.noshop.product_service.entity.Category;
import com.noshop.product_service.entity.Product;
import com.noshop.product_service.entity.ProductImage;
import com.noshop.product_service.entity.SubCategory;
import com.noshop.product_service.enums.ProductStatus;
import com.noshop.product_service.event.ProductCreatedEvent;
import com.noshop.product_service.event.ProductVariantEvent;
import com.noshop.product_service.mapper.ProductMapper;
import com.noshop.product_service.outbox.OutboxEvent;
import com.noshop.product_service.outbox.OutboxEventRepository;
import com.noshop.product_service.outbox.OutboxStatus;
import com.noshop.product_service.repository.BrandRepository;
import com.noshop.product_service.repository.CategoryRepository;
import com.noshop.product_service.repository.ProductImageRepository;
import com.noshop.product_service.repository.ProductRepository;
import com.noshop.product_service.repository.SubCategoryRepository;
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

/** Handles product lifecycle, catalog reads, search, cache invalidation, and events. */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private static final int LOW_STOCK_THRESHOLD = 5;

    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductMapper productMapper;
    private final S3Service s3Service;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    /** Creates a product and stores its integration event in the transactional outbox. */
    @Override
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        String slug = request.getSlug().trim();
        if (productRepository.existsBySlug(slug)) {
            throw new DuplicateResourceException("Product slug already exists: " + slug);
        }

        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Brand not found with id: " + request.getBrandId()));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + request.getCategoryId()));

        SubCategory subCategory = subCategoryRepository.findById(request.getSubCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "SubCategory not found with id: " + request.getSubCategoryId()));

        validateSubCategoryBelongsToCategory(subCategory, category.getId());

        Product product = productMapper.toEntity(request);
        product.setSlug(slug);
        product.setBrand(brand);
        product.setCategory(category);
        product.setSubCategory(subCategory);

        Product savedProduct = productRepository.save(product);
        createProductCreatedOutboxEvent(savedProduct);

        return productMapper.toResponse(savedProduct);
    }

    /** Returns a filtered page of products and attaches their images in one batch query. */
    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(
            Long categoryId,
            Long subCategoryId,
            ProductStatus status,
            Pageable pageable) {

        Page<Product> products;

        if (categoryId != null && subCategoryId != null && status != null) {
            products = productRepository.findByCategoryIdAndSubCategoryIdAndStatus(
                    categoryId, subCategoryId, status, pageable);
        } else if (categoryId != null && subCategoryId != null) {
            products = productRepository.findByCategoryIdAndSubCategoryId(
                    categoryId, subCategoryId, pageable);
        } else if (categoryId != null && status != null) {
            products = productRepository.findByCategoryIdAndStatus(
                    categoryId, status, pageable);
        } else if (subCategoryId != null && status != null) {
            products = productRepository.findBySubCategoryIdAndStatus(
                    subCategoryId, status, pageable);
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

    /** Returns one product and caches the mapped response for ten minutes. */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "#id")
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findByIdWithImages(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + id));

        return productMapper.toResponse(product);
    }

    /** Updates a product and evicts its cached representation. */
    @Override
    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public ProductResponse updateProduct(Long id, CreateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + id));

        String slug = request.getSlug().trim();
        if (!product.getSlug().equals(slug)
                && productRepository.existsBySlugAndIdNot(slug, id)) {
            throw new DuplicateResourceException("Product slug already exists: " + slug);
        }

        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Brand not found with id: " + request.getBrandId()));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + request.getCategoryId()));

        SubCategory subCategory = subCategoryRepository.findById(request.getSubCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "SubCategory not found with id: " + request.getSubCategoryId()));

        validateSubCategoryBelongsToCategory(subCategory, category.getId());

        productMapper.updateEntity(request, product);
        product.setSlug(slug);
        product.setBrand(brand);
        product.setCategory(category);
        product.setSubCategory(subCategory);

        return productMapper.toResponse(productRepository.save(product));
    }

    /** Deletes a product, its owned images, and associated database records. */
    @Override
    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + id));

        for (ProductImage image : product.getImages()) {
            if (image.getStorageKey() != null && !image.getStorageKey().isBlank()) {
                s3Service.deleteFile(image.getStorageKey());
            }
        }

        productRepository.delete(product);
    }

    /** Searches product names and descriptions using the repository search query. */
    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Search query must not be blank");
        }

        return mapProductsWithImages(
                productRepository.searchProducts(query.trim(), pageable));
    }

    /** Changes product status while preventing a discontinued product from being reactivated. */
    @Override
    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public ProductResponse updateProductStatus(Long id, ProductStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Product status is required");
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + id));

        if (product.getStatus() == ProductStatus.DISCONTINUED
                && status != ProductStatus.DISCONTINUED) {
            throw new OperationNotAllowedException(
                    "Discontinued product cannot be reactivated");
        }

        product.setStatus(status);
        return productMapper.toResponse(productRepository.save(product));
    }

    /** Writes the product-created event to the database outbox in the same transaction as the product. */
    private void createProductCreatedOutboxEvent(Product product) {
        ProductCreatedEvent event = ProductCreatedEvent.builder()
                .productId(product.getId())
                .name(product.getName())
                .status(product.getStatus().name())
                .variants(product.getVariants().stream()
                        .map(variant -> ProductVariantEvent.builder()
                                .variantId(variant.getId())
                                .sku(variant.getSku())
                                .packSize(variant.getPackSize())
                                .unit(variant.getUnit())
                                .price(variant.getPrice())
                                .build())
                        .toList())
                .build();

        try {
            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .eventType("PRODUCT_CREATED")
                    .aggregateId(product.getId())
                    .payload(objectMapper.writeValueAsString(event))
                    .status(OutboxStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build();

            outboxEventRepository.save(outboxEvent);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(
                    "Failed to serialize product-created event", ex);
        }
    }

    /** Ensures the selected subcategory actually belongs to the selected category. */
    private void validateSubCategoryBelongsToCategory(
            SubCategory subCategory,
            Long categoryId) {
        if (!subCategory.getCategory().getId().equals(categoryId)) {
            throw new IllegalArgumentException(
                    "SubCategory does not belong to the selected category");
        }
    }

    /** Maps a page of products and batches image lookup to avoid N+1 image queries. */
    private Page<ProductResponse> mapProductsWithImages(Page<Product> products) {
        List<Long> productIds = products.getContent().stream()
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
                    product.getId(), Collections.emptyList());

            response.setImages(images.stream()
                    .map(productMapper::toImageResponse)
                    .toList());

            return response;
        });
    }
}
