package com.noshop.inventory_service.controller;

import com.noshop.inventory_service.dto.request.InventoryRequest;
import com.noshop.inventory_service.dto.response.InventoryResponse;
import com.noshop.inventory_service.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
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
    @ResponseStatus(HttpStatus.OK)
    public InventoryResponse getInventory(
            @RequestParam Long variantId,
            @RequestParam Long warehouseId
    ) {
        return inventoryService.getInventory(
                variantId,
                warehouseId
        );
    }
    @PostMapping("/stock/add")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addStock(
            @RequestParam Long variantId,
            @RequestParam Long warehouseId,
            @RequestParam Integer quantity
    ) {
        inventoryService.addStock(
                variantId,
                warehouseId,
                quantity
        );
    }

    @PostMapping("/stock/reduce")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reduceStock(
            @RequestParam Long variantId,
            @RequestParam Long warehouseId,
            @RequestParam Integer quantity
    ) {
        inventoryService.reduceStock(
                variantId,
                warehouseId,
                quantity
        );
    }

    @PostMapping("/stock/reserve")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reserveStock(
            @RequestParam Long variantId,
            @RequestParam Long warehouseId,
            @RequestParam Integer quantity
    ) {
        inventoryService.reserveStock(
                variantId,
                warehouseId,
                quantity
        );
    }

    @PostMapping("/stock/release")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void releaseStock(
            @RequestParam Long variantId,
            @RequestParam Long warehouseId,
            @RequestParam Integer quantity
    ) {
        inventoryService.releaseStock(
                variantId,
                warehouseId,
                quantity
        );
    }
}