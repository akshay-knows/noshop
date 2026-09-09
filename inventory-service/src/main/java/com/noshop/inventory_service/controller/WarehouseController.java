package com.noshop.inventory_service.controller;

import com.noshop.inventory_service.dto.request.WarehouseRequest;
import com.noshop.inventory_service.dto.response.WarehouseResponse;
import com.noshop.inventory_service.service.WarehouseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WarehouseResponse createWarehouse(
            @Valid @RequestBody WarehouseRequest request
    ) {
        return warehouseService.createWarehouse(request);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public WarehouseResponse getWarehouse(
            @PathVariable Long id
    ) {
        return warehouseService.getWarehouse(id);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<WarehouseResponse> getAllWarehouses() {
        return warehouseService.getAllWarehouses();
    }

    @PatchMapping("/{id}/activate")
    @ResponseStatus(HttpStatus.OK)
    public WarehouseResponse activateWarehouse(
            @PathVariable Long id
    ) {
        return warehouseService.activateWarehouse(id);
    }

    @PatchMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.OK)
    public WarehouseResponse deactivateWarehouse(
            @PathVariable Long id
    ) {
        return warehouseService.deactivateWarehouse(id);
    }
}