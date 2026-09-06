package com.noshop.product_service.service;

import com.noshop.product_service.dto.request.CreateSizeRequest;
import com.noshop.product_service.dto.response.SizeResponse;

import java.util.List;

public interface SizeService {

    SizeResponse createSize(CreateSizeRequest request);

    SizeResponse getSizeById(Long id);

    List<SizeResponse> getAllSizes();

    SizeResponse updateSize(Long id, CreateSizeRequest request);

    void deleteSize(Long id);

}