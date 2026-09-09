package com.noshop.inventory_service.controller;

import com.noshop.inventory_service.dto.request.InventoryRequest;
import com.noshop.inventory_service.dto.request.InventoryUpdateRequest;
import com.noshop.inventory_service.dto.response.InventoryResponse;
import com.noshop.inventory_service.service.InventoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Validated
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryResponse createInventory(
            @Valid @RequestBody InventoryRequest request
    ) {
        return inventoryService.createInventory(request);
    }

    @GetMapping
    public InventoryResponse getInventory(
            @RequestParam @Positive Long variantId,
            @RequestParam @Positive Long warehouseId
    ) {
        return inventoryService.getInventory(variantId, warehouseId);
    }

    @GetMapping("/variant/{variantId}")
    public List<InventoryResponse> getInventoryByVariant(
            @PathVariable @Positive Long variantId
    ) {
        return inventoryService.getInventoryByVariant(variantId);
    }

    @GetMapping("/warehouse/{warehouseId}")
    public List<InventoryResponse> getInventoryByWarehouse(
            @PathVariable @Positive Long warehouseId
    ) {
        return inventoryService.getInventoryByWarehouse(warehouseId);
    }

    @PutMapping("/{id}")
    public InventoryResponse updateInventory(
            @PathVariable @Positive Long id,
            @Valid @RequestBody InventoryUpdateRequest request
    ) {
        return inventoryService.updateInventory(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteInventory(
            @PathVariable @Positive Long id
    ) {
        inventoryService.deleteInventory(id);
    }

    @PostMapping("/stock/add")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addStock(
            @RequestParam @Positive Long variantId,
            @RequestParam @Positive Long warehouseId,
            @RequestParam @Positive Integer quantity
    ) {
        inventoryService.addStock(variantId, warehouseId, quantity);
    }

    @PostMapping("/stock/reduce")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reduceStock(
            @RequestParam @Positive Long variantId,
            @RequestParam @Positive Long warehouseId,
            @RequestParam @Positive Integer quantity
    ) {
        inventoryService.reduceStock(variantId, warehouseId, quantity);
    }

    @PostMapping("/stock/reserve")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reserveStock(
            @RequestParam @Positive Long variantId,
            @RequestParam @Positive Long warehouseId,
            @RequestParam @Positive Integer quantity
    ) {
        inventoryService.reserveStock(variantId, warehouseId, quantity);
    }

    @PostMapping("/stock/release")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void releaseStock(
            @RequestParam @Positive Long variantId,
            @RequestParam @Positive Long warehouseId,
            @RequestParam @Positive Integer quantity
    ) {
        inventoryService.releaseStock(variantId, warehouseId, quantity);
    }
}
