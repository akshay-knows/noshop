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

/**
 * REST endpoints for inventory records and stock operations.
 */
@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Validated
public class InventoryController {

    private final InventoryService inventoryService;

    /** Creates an inventory record for a variant and warehouse. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryResponse createInventory(@Valid @RequestBody InventoryRequest request) {
        return inventoryService.createInventory(request);
    }

    /** Returns inventory for a variant in a warehouse. */
    @GetMapping
    public InventoryResponse getInventory(
            @RequestParam @Positive Long variantId,
            @RequestParam @Positive Long warehouseId) {
        return inventoryService.getInventory(variantId, warehouseId);
    }

    /** Returns all warehouse inventory records for a variant. */
    @GetMapping("/variant/{variantId}")
    public List<InventoryResponse> getInventoryByVariant(@PathVariable @Positive Long variantId) {
        return inventoryService.getInventoryByVariant(variantId);
    }

    /** Returns all inventory records stored in a warehouse. */
    @GetMapping("/warehouse/{warehouseId}")
    public List<InventoryResponse> getInventoryByWarehouse(@PathVariable @Positive Long warehouseId) {
        return inventoryService.getInventoryByWarehouse(warehouseId);
    }

    /** Updates the physical quantity of an inventory record. */
    @PutMapping("/{id}")
    public InventoryResponse updateInventory(
            @PathVariable @Positive Long id,
            @Valid @RequestBody InventoryUpdateRequest request) {
        return inventoryService.updateInventory(id, request);
    }

    /** Deletes an inventory record when no stock is reserved. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteInventory(@PathVariable @Positive Long id) {
        inventoryService.deleteInventory(id);
    }

    /** Adds stock to an inventory record. */
    @PostMapping("/stock/add")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addStock(
            @RequestParam @Positive Long variantId,
            @RequestParam @Positive Long warehouseId,
            @RequestParam @Positive Integer quantity) {
        inventoryService.addStock(variantId, warehouseId, quantity);
    }

    /** Reduces available stock from an inventory record. */
    @PostMapping("/stock/reduce")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reduceStock(
            @RequestParam @Positive Long variantId,
            @RequestParam @Positive Long warehouseId,
            @RequestParam @Positive Integer quantity) {
        inventoryService.reduceStock(variantId, warehouseId, quantity);
    }

    /** Reserves available stock for a downstream order workflow. */
    @PostMapping("/stock/reserve")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reserveStock(
            @RequestParam @Positive Long variantId,
            @RequestParam @Positive Long warehouseId,
            @RequestParam @Positive Integer quantity) {
        inventoryService.reserveStock(variantId, warehouseId, quantity);
    }

    /** Releases previously reserved stock. */
    @PostMapping("/stock/release")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void releaseStock(
            @RequestParam @Positive Long variantId,
            @RequestParam @Positive Long warehouseId,
            @RequestParam @Positive Integer quantity) {
        inventoryService.releaseStock(variantId, warehouseId, quantity);
    }
}
