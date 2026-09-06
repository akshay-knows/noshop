package com.noshop.product_service.service.impl;

import com.noshop.common.exception.ResourceNotFoundException;
import com.noshop.product_service.dto.request.CreateBrandRequest;
import com.noshop.product_service.dto.response.BrandResponse;
import com.noshop.product_service.entity.Brand;
import com.noshop.product_service.mapper.BrandMapper;
import com.noshop.product_service.repository.BrandRepository;
import com.noshop.product_service.service.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class BrandServiceImpl implements BrandService {
    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;

    @Override
    public BrandResponse createBrand(CreateBrandRequest request) {
        if (brandRepository.existsByName(request.getName())) {
            throw new RuntimeException("Brand already exist");
        }
        Brand brand = Brand.builder()
                           .name(request.getName())
                           .description(request.getDescription())
                           .logoUrl(request.getLogoUrl())
                           .active(true)
                           .build();

        Brand savedBrand = brandRepository.save(brand);
        return brandMapper.toResponse(savedBrand);
    }

    @Override
    public BrandResponse getBrandById(Long id) {
        Brand brand = brandRepository.findById(id)
                                     .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id : " + id));
        return brandMapper.toResponse(brand);
    }

    @Override
    public List<BrandResponse> getAllBrands() {
        return brandRepository.findAll()
                              .stream()
                              .map(brandMapper::toResponse)
                              .toList();
    }

    @Override
    public BrandResponse updateBrand(Long id,
                                     CreateBrandRequest request) {

        Brand brand = brandRepository.findById(id)
                                     .orElseThrow(() -> new ResourceNotFoundException("Brand not " + "found with id: " + id));
        if (!brand.getName()
                  .equals(request.getName()) && brandRepository.existsByName(request.getName())) {
            throw new RuntimeException("Brand already exists with name: " + request.getName());

        }
        brand.setName(request.getName());
        brand.setLogoUrl(request.getLogoUrl());
        brand.setDescription(request.getDescription());
        Brand updatedBrand = brandRepository.save(brand);

        return brandMapper.toResponse(updatedBrand);
    }
    @Override
    public void deleteBrand(Long id) {

        Brand brand = brandRepository.findById(id)
                                     .orElseThrow(() ->
                                                          new ResourceNotFoundException(
                                                                  "Brand not found with id: " + id));

        brand.setActive(false);

        brandRepository.save(brand);
    }

}


