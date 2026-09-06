package com.noshop.product_service.service;

import com.noshop.product_service.dto.request.CreateBrandRequest;
import com.noshop.product_service.dto.response.BrandResponse;

import java.util.List;

public interface BrandService {

    BrandResponse createBrand(CreateBrandRequest request);

    BrandResponse getBrandById(Long id);

    List<BrandResponse> getAllBrands();

    BrandResponse updateBrand(Long id,
                              CreateBrandRequest request);

    void deleteBrand(Long id);
}
