package com.noshop.product_service.service.impl;

import com.noshop.common.exception.DuplicateResourceException;
import com.noshop.common.exception.ResourceNotFoundException;
import com.noshop.product_service.dto.request.CreateBrandRequest;
import com.noshop.product_service.dto.response.BrandResponse;
import com.noshop.product_service.entity.Brand;
import com.noshop.product_service.mapper.BrandMapper;
import com.noshop.product_service.repository.BrandRepository;
import com.noshop.product_service.service.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Manages brand creation, lookup, updates, and soft deletion. */
@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;

    /** Creates a unique active brand from the supplied request. */
    @Override
    @Transactional
    public BrandResponse createBrand(CreateBrandRequest request) {
        if (brandRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException(
                    "Brand already exists with name: " + request.getName());
        }

        Brand brand = Brand.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .logoUrl(request.getLogoUrl())
                .active(true)
                .build();

        return brandMapper.toResponse(brandRepository.save(brand));
    }

    /** Returns one brand or fails when the identifier does not exist. */
    @Override
    @Transactional(readOnly = true)
    public BrandResponse getBrandById(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Brand not found with id: " + id));
        return brandMapper.toResponse(brand);
    }

    /** Returns all brands currently stored in the catalog. */
    @Override
    @Transactional(readOnly = true)
    public List<BrandResponse> getAllBrands() {
        return brandRepository.findAll()
                .stream()
                .map(brandMapper::toResponse)
                .toList();
    }

    /** Updates brand details while preserving name uniqueness. */
    @Override
    @Transactional
    public BrandResponse updateBrand(Long id, CreateBrandRequest request) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Brand not found with id: " + id));

        String name = request.getName().trim();
        if (!brand.getName().equals(name) && brandRepository.existsByName(name)) {
            throw new DuplicateResourceException(
                    "Brand already exists with name: " + name);
        }

        brand.setName(name);
        brand.setLogoUrl(request.getLogoUrl());
        brand.setDescription(request.getDescription());

        return brandMapper.toResponse(brandRepository.save(brand));
    }

    /** Soft-deletes a brand by marking it inactive. */
    @Override
    @Transactional
    public void deleteBrand(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Brand not found with id: " + id));

        brand.setActive(false);
        brandRepository.save(brand);
    }
}
