package com.noshop.inventory_service.service;

import com.noshop.inventory_service.dto.request.WarehouseRequest;
import com.noshop.inventory_service.dto.response.WarehouseResponse;

import java.util.List;

public interface WarehouseService {

    WarehouseResponse createWarehouse(WarehouseRequest request);

    WarehouseResponse getWarehouse(Long id);

    List<WarehouseResponse> getAllWarehouses();

    WarehouseResponse activateWarehouse(Long id);

    WarehouseResponse deactivateWarehouse(Long id);
}