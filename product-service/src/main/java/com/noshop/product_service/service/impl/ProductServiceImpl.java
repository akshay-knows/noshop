package com.noshop.product_service.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.noshop.common.exception.DuplicateResourceException;
import com.noshop.common.exception.OperationNotAllowedException;
import com.noshop.common.exception.ResourceNotFoundException;
import com.noshop.product_service.dto.request.CreateProductRequest;
import com.noshop.product_service.dto.response.ProductRecommendationResponse;
import com.noshop.product_service.dto.response.ProductResponse;
import com.noshop.product_service.entity.Brand;
import com.noshop.product_service.entity.Category;
import com.noshop.product_service.entity.Product;
import com.noshop.product_service.entity.ProductImage;
import com.noshop.product_service.entity.ProductVariant;
import com.noshop.product_service.entity.SubCategory;
import com.noshop.product_service.enums.CatalogAudience;
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
import com.noshop.product_service.repository.ProductRecommendationCandidate;
import com.noshop.product_service.repository.ProductRepository;
import com.noshop.product_service.repository.ProductVariantRepository;
import com.noshop.product_service.repository.SubCategoryRepository;
import com.noshop.product_service.security.CatalogAudienceResolver;
import com.noshop.product_service.service.ProductService;
import com.noshop.product_service.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Handles product lifecycle, catalog reads, search, caching, recommendations,
 * and integration events.
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductMapper productMapper;
    private final S3Service s3Service;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final CatalogAudienceResolver audienceResolver;

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
        product.setAudience(normalizeAudience(request.getAudience()));

        Product savedProduct = productRepository.save(product);
        createProductCreatedOutboxEvent(savedProduct);

        return toProductResponse(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(
            Long categoryId,
            Long subCategoryId,
            ProductStatus status,
            Pageable pageable) {

        Page<Product> products = productRepository.findCatalogProducts(
                audienceResolver.allowedAudiences(),
                categoryId,
                subCategoryId,
                status,
                pageable);

        return mapProductsWithImages(products);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = "products",
            keyGenerator = "productCacheKeyGenerator"
    )
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findByIdWithImages(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + id));

        if (!audienceResolver.isVisible(product.getAudience())) {
            throw new ResourceNotFoundException(
                    "Product not available for the current customer segment");
        }

        return toProductResponse(product);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
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
        product.setAudience(normalizeAudience(request.getAudience()));

        return toProductResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
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

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Search query must not be blank");
        }

        return mapProductsWithImages(
                productRepository.searchCatalogProducts(
                        audienceResolver.allowedAudiences(),
                        query.trim(),
                        pageable));
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
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
        return toProductResponse(productRepository.save(product));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductRecommendationResponse> getSubstitutes(Long id, int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 10);

        Product original = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + id));

        if (!audienceResolver.isVisible(original.getAudience())) {
            throw new ResourceNotFoundException(
                    "Product not available for the current customer segment");
        }

        BigDecimal originalPrice = productVariantRepository.findByProductId(id)
                .stream()
                .filter(variant -> variant.getStatus() == ProductStatus.ACTIVE)
                .map(ProductVariant::getPrice)
                .filter(price -> price != null)
                .min(Comparator.naturalOrder())
                .orElse(null);

        if (originalPrice == null) {
            return Collections.emptyList();
        }

        Set<CatalogAudience> allowedAudiences = audienceResolver.allowedAudiences();

        return productRepository.findRecommendationCandidates(
                        allowedAudiences,
                        original.getCategory().getId(),
                        id,
                        ProductStatus.ACTIVE)
                .stream()
                .sorted(Comparator.comparing(
                        candidate -> candidate.getPrice()
                                .subtract(originalPrice)
                                .abs()))
                .limit(safeLimit)
                .map(candidate -> ProductRecommendationResponse.builder()
                        .productId(candidate.getId())
                        .name(candidate.getName())
                        .slug(candidate.getSlug())
                        .price(candidate.getPrice())
                        .priceDifference(
                                candidate.getPrice()
                                        .subtract(originalPrice)
                                        .abs())
                        .build())
                .toList();
    }

    private CatalogAudience normalizeAudience(CatalogAudience audience) {
        return audience == null ? CatalogAudience.BOTH : audience;
    }

    private ProductResponse toProductResponse(Product product) {
        ProductResponse response = productMapper.toResponse(product);
        if (response.getAudience() == null) {
            response.setAudience(CatalogAudience.BOTH);
        }
        return response;
    }

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

    private void validateSubCategoryBelongsToCategory(
            SubCategory subCategory,
            Long categoryId) {
        if (!subCategory.getCategory().getId().equals(categoryId)) {
            throw new IllegalArgumentException(
                    "SubCategory does not belong to the selected category");
        }
    }

    private Page<ProductResponse> mapProductsWithImages(Page<Product> products) {
        List<Long> productIds = products.getContent().stream()
                .map(Product::getId)
                .toList();

        if (productIds.isEmpty()) {
            return products.map(this::toProductResponse);
        }

        Map<Long, List<ProductImage>> imagesByProductId =
                productImageRepository.findByProductIdInOrderByDisplayOrderAsc(productIds)
                        .stream()
                        .collect(Collectors.groupingBy(
                                image -> image.getProduct().getId()));

        return products.map(product -> {
            ProductResponse response = toProductResponse(product);

            List<ProductImage> images = imagesByProductId.getOrDefault(
                    product.getId(),
                    Collections.emptyList());

            response.setImages(images.stream()
                    .map(productMapper::toImageResponse)
                    .toList());

            return response;
        });
    }
}
