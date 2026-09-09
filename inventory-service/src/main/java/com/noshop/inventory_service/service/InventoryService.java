package com.noshop.inventory_service.service;

import com.noshop.inventory_service.dto.request.InventoryRequest;
import com.noshop.inventory_service.dto.response.InventoryResponse;

public interface InventoryService {

    InventoryResponse createInventory(InventoryRequest request);

    InventoryResponse getInventory(Long variantId, Long warehouseId);

    void addStock(Long variantId, Long warehouseId, Integer quantity);

    void reduceStock(Long variantId, Long warehouseId, Integer quantity);

    void reserveStock(Long variantId, Long warehouseId, Integer quantity);

    void releaseStock(Long variantId, Long warehouseId, Integer quantity);
}