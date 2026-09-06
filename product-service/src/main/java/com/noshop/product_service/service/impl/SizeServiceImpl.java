package com.noshop.product_service.service.impl;

import com.noshop.common.exception.ResourceNotFoundException;
import com.noshop.product_service.dto.request.CreateSizeRequest;
import com.noshop.product_service.dto.response.SizeResponse;
import com.noshop.product_service.entity.Size;
import com.noshop.product_service.mapper.SizeMapper;
import com.noshop.product_service.repository.SizeRepository;
import com.noshop.product_service.service.SizeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SizeServiceImpl implements SizeService {

    private final SizeRepository sizeRepository;
    private final SizeMapper sizeMapper;

    @Override
    public SizeResponse createSize(CreateSizeRequest request) {

        if (sizeRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Size already exists.");
        }

        Size size = sizeMapper.toEntity(request);

        return sizeMapper.toResponse(
                sizeRepository.save(size)
        );
    }

    @Override
    public SizeResponse getSizeById(Long id) {

        Size size = sizeRepository.findById(id)
                                  .orElseThrow(() ->
                                                       new ResourceNotFoundException("Size not found."));

        return sizeMapper.toResponse(size);
    }

    @Override
    public List<SizeResponse> getAllSizes() {

        return sizeRepository.findAll()
                             .stream()
                             .map(sizeMapper::toResponse)
                             .toList();
    }

    @Override
    public SizeResponse updateSize(Long id, CreateSizeRequest request) {

        Size size = sizeRepository.findById(id)
                                  .orElseThrow(() ->
                                                       new ResourceNotFoundException("Size not found."));

        size.setName(request.getName());

        return sizeMapper.toResponse(
                sizeRepository.save(size)
        );
    }

    @Override
    public void deleteSize(Long id) {

        Size size = sizeRepository.findById(id)
                                  .orElseThrow(() ->
                                                       new ResourceNotFoundException("Size not found."));

        sizeRepository.delete(size);
    }
}