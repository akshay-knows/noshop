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

/** Manages warehouse creation, lookup, activation, and deactivation. */
@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;

    /** Creates a warehouse with a unique business code and active status. */
    @Override
    @Transactional
    public WarehouseResponse createWarehouse(WarehouseRequest request) {
        String code = request.getCode().trim();

        if (warehouseRepository.existsByCode(code)) {
            throw new DuplicateWarehouseException(
                    "Warehouse already exists with code: " + code);
        }

        Warehouse warehouse = Warehouse.builder()
                .code(code)
                .name(request.getName().trim())
                .city(request.getCity().trim())
                .address(request.getAddress())
                .active(true)
                .build();

        return toResponse(warehouseRepository.save(warehouse));
    }

    /** Returns one warehouse by identifier. */
    @Override
    @Transactional(readOnly = true)
    public WarehouseResponse getWarehouse(Long id) {
        return toResponse(findWarehouse(id));
    }

    /** Returns all configured warehouses. */
    @Override
    @Transactional(readOnly = true)
    public List<WarehouseResponse> getAllWarehouses() {
        return warehouseRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /** Activates a warehouse for normal inventory operations. */
    @Override
    @Transactional
    public WarehouseResponse activateWarehouse(Long id) {
        Warehouse warehouse = findWarehouse(id);
        warehouse.setActive(true);
        return toResponse(warehouseRepository.save(warehouse));
    }

    /** Deactivates a warehouse without deleting its inventory history. */
    @Override
    @Transactional
    public WarehouseResponse deactivateWarehouse(Long id) {
        Warehouse warehouse = findWarehouse(id);
        warehouse.setActive(false);
        return toResponse(warehouseRepository.save(warehouse));
    }

    /** Finds a warehouse or raises a domain-specific not-found exception. */
    private Warehouse findWarehouse(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new WarehouseNotFoundException(
                        "Warehouse not found: " + id));
    }

    /** Maps the persistence entity to the public warehouse response. */
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
