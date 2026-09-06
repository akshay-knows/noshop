package com.noshop.product_service.service.impl;

import com.noshop.common.exception.ResourceNotFoundException;
import com.noshop.product_service.dto.request.CreateProductRequest;
import com.noshop.product_service.dto.response.ProductResponse;
import com.noshop.product_service.entity.Brand;
import com.noshop.product_service.entity.Category;
import com.noshop.product_service.entity.Product;
import com.noshop.product_service.entity.SubCategory;
import com.noshop.product_service.mapper.ProductMapper;
import com.noshop.product_service.repository.BrandRepository;
import com.noshop.product_service.repository.CategoryRepository;
import com.noshop.product_service.repository.ProductRepository;
import com.noshop.product_service.repository.SubCategoryRepository;
import com.noshop.product_service.service.ProductService;
import com.noshop.product_service.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {


    private final ProductRepository productRepository;

    private final BrandRepository brandRepository;

    private final CategoryRepository categoryRepository;

    private final SubCategoryRepository subCategoryRepository;

    private final ProductMapper productMapper;

    private final S3Service s3Service;


    @Override
    public ProductResponse createProduct(CreateProductRequest request) {

        Brand brand = brandRepository.findById(request.getBrandId())
                                     .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + request.getBrandId()));


        Category category = categoryRepository.findById(request.getCategoryId())
                                              .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));


        SubCategory subCategory = subCategoryRepository.findById(request.getSubCategoryId())
                                                       .orElseThrow(() -> new ResourceNotFoundException("SubCategory not found with id: " + request.getSubCategoryId()));


        Product product = productMapper.toEntity(request);

        product.setBrand(brand);
        product.setCategory(category);
        product.setSubCategory(subCategory);
        Product savedProduct = productRepository.save(product);
        return productMapper.toResponse(savedProduct);
    }


    @Override
    public List<ProductResponse> getAllProducts() {

        return productRepository.findAll()
                                .stream()
                                .map(productMapper::toResponse)
                                .toList();
    }


    @Override
    public ProductResponse getProductById(Long id) {

        Product product = productRepository.findById(id)
                                           .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));


        return productMapper.toResponse(product);
    }


    @Override
    public ProductResponse updateProduct(Long id,
                                         CreateProductRequest request) {


        Product product = productRepository.findById(id)
                                           .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));


        Brand brand = brandRepository.findById(request.getBrandId())
                                     .orElseThrow(() -> new ResourceNotFoundException("Brand not found"));


        Category category = categoryRepository.findById(request.getCategoryId())
                                              .orElseThrow(() -> new ResourceNotFoundException("Category not found"));


        SubCategory subCategory = subCategoryRepository.findById(request.getSubCategoryId())
                                                       .orElseThrow(() -> new ResourceNotFoundException("SubCategory not found"));


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
                                           .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));


        productRepository.delete(product);
    }
}