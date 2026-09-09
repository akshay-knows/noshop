package com.noshop.inventory_service.service;

import com.noshop.inventory_service.dto.request.InventoryRequest;
import com.noshop.inventory_service.dto.request.InventoryUpdateRequest;
import com.noshop.inventory_service.dto.response.InventoryResponse;

import java.util.List;

/**
 * Defines inventory management operations for warehouse stock.
 *
 * <p>The service manages inventory records, stock movements, reservations,
 * releases, and inventory lookup operations.</p>
 */
public interface InventoryService {

    /** Creates an inventory record for a product variant in a warehouse. */
    InventoryResponse createInventory(InventoryRequest request);

    /** Returns inventory for a variant in a specific warehouse. */
    InventoryResponse getInventory(Long variantId, Long warehouseId);

    /** Returns inventory records belonging to a product variant. */
    List<InventoryResponse> getInventoryByVariant(Long variantId);

    /** Returns inventory records belonging to a warehouse. */
    List<InventoryResponse> getInventoryByWarehouse(Long warehouseId);

    /** Updates the physical quantity of an inventory record. */
    InventoryResponse updateInventory(Long id, InventoryUpdateRequest request);

    /** Deletes an inventory record when no stock is reserved. */
    void deleteInventory(Long id);

    /** Adds available stock to an inventory record. */
    void addStock(Long variantId, Long warehouseId, Integer quantity);

    /** Reduces available stock from an inventory record. */
    void reduceStock(Long variantId, Long warehouseId, Integer quantity);

    /** Reserves available stock without reducing physical quantity. */
    void reserveStock(Long variantId, Long warehouseId, Integer quantity);

    /** Releases previously reserved stock back to available stock. */
    void releaseStock(Long variantId, Long warehouseId, Integer quantity);
}
