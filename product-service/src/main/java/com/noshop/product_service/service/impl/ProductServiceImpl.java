package com.noshop.product_service.service.impl;

import com.noshop.common.exception.ResourceNotFoundException;
import com.noshop.product_service.dto.request.CreateProductRequest;
import com.noshop.product_service.dto.response.ProductResponse;
import com.noshop.product_service.entity.Brand;
import com.noshop.product_service.entity.Category;
import com.noshop.product_service.entity.Product;
import com.noshop.product_service.entity.ProductImage;
import com.noshop.product_service.entity.SubCategory;
import com.noshop.product_service.enums.ProductStatus;
import com.noshop.product_service.mapper.ProductMapper;
import com.noshop.product_service.repository.BrandRepository;
import com.noshop.product_service.repository.CategoryRepository;
import com.noshop.product_service.repository.ProductImageRepository;
import com.noshop.product_service.repository.ProductRepository;
import com.noshop.product_service.repository.SubCategoryRepository;
import com.noshop.product_service.service.ProductService;
import com.noshop.product_service.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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


    @Override
    public ProductResponse createProduct(CreateProductRequest request) {

        if (productRepository.existsBySlug(request.getSlug())) {
            throw new RuntimeException("Product slug already exists");
        }

        Brand brand = brandRepository.findById(request.getBrandId())
                                     .orElseThrow(() ->
                                                          new ResourceNotFoundException(
                                                                  "Brand not found with id: "
                                                                          + request.getBrandId()
                                                          )
                                     );

        Category category = categoryRepository.findById(request.getCategoryId())
                                              .orElseThrow(() ->
                                                                   new ResourceNotFoundException(
                                                                           "Category not found with id: "
                                                                                   + request.getCategoryId()
                                                                   )
                                              );

        SubCategory subCategory = subCategoryRepository.findById(
                                                               request.getSubCategoryId()
                                                       )
                                                       .orElseThrow(() ->
                                                                            new ResourceNotFoundException(
                                                                                    "SubCategory not found with id: "
                                                                                            + request.getSubCategoryId()
                                                                            )
                                                       );

        Product product = productMapper.toEntity(request);

        product.setBrand(brand);
        product.setCategory(category);
        product.setSubCategory(subCategory);

        Product savedProduct = productRepository.save(product);

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

            products = productRepository.findByCategoryIdAndStatus(
                    categoryId,
                    status,
                    pageable
            );

        } else if (subCategoryId != null && status != null) {

            products = productRepository.findBySubCategoryIdAndStatus(
                    subCategoryId,
                    status,
                    pageable
            );

        } else if (categoryId != null) {

            products = productRepository.findByCategoryId(
                    categoryId,
                    pageable
            );

        } else if (subCategoryId != null) {

            products = productRepository.findBySubCategoryId(
                    subCategoryId,
                    pageable
            );

        } else if (status != null) {

            products = productRepository.findByStatus(
                    status,
                    pageable
            );

        } else {

            products = productRepository.findAll(pageable);
        }

        List<Long> productIds = products.getContent()
                                        .stream()
                                        .map(Product::getId)
                                        .toList();

        Map<Long, List<ProductImage>> imagesByProductId =
                productIds.isEmpty()
                        ? Collections.emptyMap()
                        : productImageRepository
                          .findByProductIdInOrderByDisplayOrderAsc(
                                  productIds
                          )
                          .stream()
                          .collect(Collectors.groupingBy(
                                  image -> image.getProduct().getId()
                          ));

        return products.map(product -> {

            ProductResponse response =
                    productMapper.toResponse(product);

            List<ProductImage> images =
                    imagesByProductId.getOrDefault(
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


    @Override
    public ProductResponse getProductById(Long id) {

        Product product = productRepository.findByIdWithImages(id)
                                           .orElseThrow(() ->
                                                                new ResourceNotFoundException(
                                                                        "Product not found with id: " + id
                                                                )
                                           );

        return productMapper.toResponse(product);
    }


    @Override
    public ProductResponse updateProduct(
            Long id,
            CreateProductRequest request) {

        Product product = productRepository.findById(id)
                                           .orElseThrow(() ->
                                                                new ResourceNotFoundException(
                                                                        "Product not found with id: " + id
                                                                )
                                           );

        if (productRepository.existsBySlugAndIdNot(
                request.getSlug(),
                id
        )) {
            throw new RuntimeException("Product slug already exists");
        }

        Brand brand = brandRepository.findById(request.getBrandId())
                                     .orElseThrow(() ->
                                                          new ResourceNotFoundException(
                                                                  "Brand not found"
                                                          )
                                     );

        Category category = categoryRepository.findById(request.getCategoryId())
                                              .orElseThrow(() ->
                                                                   new ResourceNotFoundException(
                                                                           "Category not found"
                                                                   )
                                              );

        SubCategory subCategory = subCategoryRepository.findById(
                                                               request.getSubCategoryId()
                                                       )
                                                       .orElseThrow(() ->
                                                                            new ResourceNotFoundException(
                                                                                    "SubCategory not found"
                                                                            )
                                                       );

        productMapper.updateEntity(
                request,
                product
        );

        product.setBrand(brand);
        product.setCategory(category);
        product.setSubCategory(subCategory);

        Product updatedProduct = productRepository.save(product);

        return productMapper.toResponse(updatedProduct);
    }


    @Override
    public void deleteProduct(Long id) {

        Product product = productRepository.findById(id)
                                           .orElseThrow(() ->
                                                                new ResourceNotFoundException(
                                                                        "Product not found with id: " + id
                                                                )
                                           );

        productRepository.delete(product);
    }


    @Override
    public Page<ProductResponse> searchProducts(
            String query,
            Pageable pageable) {

        return productRepository.searchProducts(
                                        query,
                                        pageable
                                )
                                .map(productMapper::toResponse);
    }


    @Override
    public ProductResponse updateProductStatus(
            Long id,
            ProductStatus status) {

        Product product = productRepository.findById(id)
                                           .orElseThrow(() ->
                                                                new ResourceNotFoundException(
                                                                        "Product not found with id: " + id
                                                                )
                                           );

        if (product.getStatus() == ProductStatus.DISCONTINUED) {
            throw new IllegalStateException(
                    "Discontinued product cannot be reactivated"
            );
        }

        product.setStatus(status);

        Product savedProduct = productRepository.save(product);

        return productMapper.toResponse(savedProduct);
    }
}