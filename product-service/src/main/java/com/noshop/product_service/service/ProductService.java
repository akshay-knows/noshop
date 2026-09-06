package com.noshop.product_service.service;

import com.noshop.product_service.dto.request.CreateProductRequest;
import com.noshop.product_service.dto.response.ProductResponse;

import java.util.List;

public interface ProductService {

    ProductResponse createProduct(CreateProductRequest request);

    List<ProductResponse> getAllProducts();

    ProductResponse getProductById(Long id);

    ProductResponse updateProduct(Long id, CreateProductRequest request);

    void deleteProduct(Long id);
}