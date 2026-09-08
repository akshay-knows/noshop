package com.noshop.product_service.service.impl;

import com.noshop.common.exception.ResourceNotFoundException;
import com.noshop.product_service.dto.request.CreateProductVariantRequest;
import com.noshop.product_service.dto.response.ProductVariantResponse;
import com.noshop.product_service.entity.Product;
import com.noshop.product_service.entity.ProductVariant;
import com.noshop.product_service.mapper.ProductMapper;
import com.noshop.product_service.repository.ProductRepository;
import com.noshop.product_service.repository.ProductVariantRepository;
import com.noshop.product_service.service.ProductVariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductVariantServiceImpl implements ProductVariantService {

    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    public ProductVariantResponse createVariant(
            Long productId,
            CreateProductVariantRequest request) {

        if (productVariantRepository.existsBySku(request.getSku())) {
            throw new IllegalArgumentException(
                    "Variant SKU already exists: " + request.getSku()
            );
        }

        Product product = productRepository.findById(productId)
                                           .orElseThrow(() ->
                                                                new ResourceNotFoundException(
                                                                        "Product not found with id: " + productId
                                                                )
                                           );

        ProductVariant variant = ProductVariant.builder()
                                               .sku(request.getSku())
                                               .packSize(request.getPackSize())
                                               .unit(request.getUnit())
                                               .price(request.getPrice())
                                               .product(product)
                                               .build();

        return productMapper.toVariantResponse(
                productVariantRepository.save(variant)
        );
    }

    @Override
    public List<ProductVariantResponse> getVariantsByProductId(
            Long productId) {

        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException(
                    "Product not found with id: " + productId
            );
        }

        return productVariantRepository
                .findByProductId(productId)
                .stream()
                .map(productMapper::toVariantResponse)
                .toList();
    }

    @Override
    public ProductVariantResponse getVariantById(Long variantId) {

        ProductVariant variant =
                productVariantRepository.findById(variantId)
                                        .orElseThrow(() ->
                                                             new ResourceNotFoundException(
                                                                     "Variant not found with id: "
                                                                             + variantId
                                                             )
                                        );

        return productMapper.toVariantResponse(variant);
    }

    @Override
    public ProductVariantResponse updateVariant(
            Long variantId,
            CreateProductVariantRequest request) {

        ProductVariant variant =
                productVariantRepository.findById(variantId)
                                        .orElseThrow(() ->
                                                             new ResourceNotFoundException(
                                                                     "Variant not found with id: "
                                                                             + variantId
                                                             )
                                        );

        if (productVariantRepository.existsBySkuAndIdNot(
                request.getSku(),
                variantId)) {

            throw new IllegalArgumentException(
                    "Variant SKU already exists: " + request.getSku()
            );
        }

        variant.setSku(request.getSku());
        variant.setPackSize(request.getPackSize());
        variant.setUnit(request.getUnit());
        variant.setPrice(request.getPrice());

        return productMapper.toVariantResponse(
                productVariantRepository.save(variant)
        );
    }

    @Override
    public void deleteVariant(Long variantId) {

        ProductVariant variant =
                productVariantRepository.findById(variantId)
                                        .orElseThrow(() ->
                                                             new ResourceNotFoundException(
                                                                     "Variant not found with id: "
                                                                             + variantId
                                                             )
                                        );

        productVariantRepository.delete(variant);
    }
}