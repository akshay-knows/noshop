package com.noshop.inventory_service.service.impl;

import com.noshop.inventory_service.dto.request.WarehouseRequest;
import com.noshop.inventory_service.dto.response.WarehouseResponse;
import com.noshop.inventory_service.entity.Warehouse;
import com.noshop.inventory_service.exception.DuplicateWarehouseException;
import com.noshop.inventory_service.exception.WarehouseNotFoundException;
import com.noshop.inventory_service.repository.WarehouseRepository;
import com.noshop.inventory_service.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;

    @Override
    @Transactional
    public WarehouseResponse createWarehouse(WarehouseRequest request) {

        if (warehouseRepository.existsByCode(request.getCode())) {
            throw new DuplicateWarehouseException(
                    "Warehouse already exists with code: " + request.getCode()
            );
        }

        Warehouse warehouse = Warehouse.builder()
                .code(request.getCode())
                .name(request.getName())
                .city(request.getCity())
                .address(request.getAddress())
                .active(true)
                .build();

        return toResponse(warehouseRepository.save(warehouse));
    }

    @Override
    @Transactional(readOnly = true)
    public WarehouseResponse getWarehouse(Long id) {

        Warehouse warehouse = findWarehouse(id);

        return toResponse(warehouse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarehouseResponse> getAllWarehouses() {

        return warehouseRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public WarehouseResponse activateWarehouse(Long id) {

        Warehouse warehouse = findWarehouse(id);

        warehouse.setActive(true);

        return toResponse(warehouseRepository.save(warehouse));
    }

    @Override
    @Transactional
    public WarehouseResponse deactivateWarehouse(Long id) {

        Warehouse warehouse = findWarehouse(id);

        warehouse.setActive(false);

        return toResponse(warehouseRepository.save(warehouse));
    }

    private Warehouse findWarehouse(Long id) {

        return warehouseRepository.findById(id)
                .orElseThrow(() ->
                        new WarehouseNotFoundException(
                                "Warehouse not found: " + id
                        )
                );
    }

    private WarehouseResponse toResponse(Warehouse warehouse) {

        return WarehouseResponse.builder()
                .id(warehouse.getId())
                .code(warehouse.getCode())
                .name(warehouse.getName())
                .city(warehouse.getCity())
                .address(warehouse.getAddress())
                .active(warehouse.getActive())
                .createdAt(warehouse.getCreatedAt())
                .updatedAt(warehouse.getUpdatedAt())
                .build();
    }
}
