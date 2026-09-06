package com.noshop.product_service.service.impl;

import com.noshop.common.exception.ResourceNotFoundException;
import com.noshop.product_service.dto.request.CreateProductVariantRequest;
import com.noshop.product_service.dto.response.ProductVariantResponse;
import com.noshop.product_service.entity.Product;
import com.noshop.product_service.entity.ProductVariant;
import com.noshop.product_service.entity.Size;
import com.noshop.product_service.mapper.ProductVariantMapper;
import com.noshop.product_service.repository.ProductRepository;
import com.noshop.product_service.repository.ProductVariantRepository;
import com.noshop.product_service.repository.SizeRepository;
import com.noshop.product_service.service.ProductVariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductVariantServiceImpl implements ProductVariantService {

    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;
    private final SizeRepository sizeRepository;
    private final ProductVariantMapper productVariantMapper;

    @Override
    public ProductVariantResponse createProductVariant(CreateProductVariantRequest request) {

        if (productVariantRepository.existsBySku(request.getSku())) {
            throw new IllegalArgumentException("SKU already exists.");
        }

        Product product = productRepository.findById(request.getProductId())
                                           .orElseThrow(() ->
                                                                new ResourceNotFoundException("Product not found."));

        Size size = sizeRepository.findById(request.getSizeId())
                                  .orElseThrow(() ->
                                                       new ResourceNotFoundException("Size not found."));

        ProductVariant productVariant = productVariantMapper.toEntity(request);
        productVariant.setProduct(product);
        productVariant.setSize(size);

        return productVariantMapper.toResponse(
                productVariantRepository.save(productVariant)
        );
    }

    @Override
    public ProductVariantResponse getProductVariantById(Long id) {

        ProductVariant productVariant = productVariantRepository.findById(id)
                                                                .orElseThrow(() ->
                                                                                     new ResourceNotFoundException("Product Variant not found."));

        return productVariantMapper.toResponse(productVariant);
    }

    @Override
    public List<ProductVariantResponse> getAllProductVariants() {

        return productVariantRepository.findAll()
                                       .stream()
                                       .map(productVariantMapper::toResponse)
                                       .toList();
    }

    @Override
    public ProductVariantResponse updateProductVariant(Long id,
                                                       CreateProductVariantRequest request) {

        ProductVariant productVariant = productVariantRepository.findById(id)
                                                                .orElseThrow(() ->
                                                                                     new ResourceNotFoundException("Product Variant not found."));

        Product product = productRepository.findById(request.getProductId())
                                           .orElseThrow(() ->
                                                                new ResourceNotFoundException("Product not found."));

        Size size = sizeRepository.findById(request.getSizeId())
                                  .orElseThrow(() ->
                                                       new ResourceNotFoundException("Size not found."));

        productVariant.setSku(request.getSku());
        productVariant.setColor(request.getColor());
        productVariant.setMrp(request.getMrp());
        productVariant.setSellingPrice(request.getSellingPrice());
        productVariant.setProduct(product);
        productVariant.setSize(size);

        return productVariantMapper.toResponse(
                productVariantRepository.save(productVariant)
        );
    }

    @Override
    public void deleteProductVariant(Long id) {

        ProductVariant productVariant = productVariantRepository.findById(id)
                                                                .orElseThrow(() ->
                                                                                     new ResourceNotFoundException("Product Variant not found."));

        productVariantRepository.delete(productVariant);
    }
}