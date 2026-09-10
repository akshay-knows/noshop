package com.noshop.inventory_service.service.impl;

import com.noshop.inventory_service.dto.request.InventoryRequest;
import com.noshop.inventory_service.dto.request.InventoryUpdateRequest;
import com.noshop.inventory_service.dto.response.InventoryResponse;
import com.noshop.inventory_service.entity.Inventory;
import com.noshop.inventory_service.entity.Warehouse;
import com.noshop.inventory_service.exception.DuplicateInventoryException;
import com.noshop.inventory_service.exception.InactiveWarehouseException;
import com.noshop.inventory_service.exception.InsufficientStockException;
import com.noshop.inventory_service.exception.InvalidInventoryOperationException;
import com.noshop.inventory_service.exception.InventoryNotFoundException;
import com.noshop.inventory_service.exception.WarehouseNotFoundException;
import com.noshop.inventory_service.repository.InventoryRepository;
import com.noshop.inventory_service.repository.WarehouseRepository;
import com.noshop.inventory_service.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Manages warehouse inventory quantities, reservations, and stock availability. */
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private static final int LOW_STOCK_THRESHOLD = 5;

    private final InventoryRepository inventoryRepository;
    private final WarehouseRepository warehouseRepository;

    /** Creates a zero-reservation inventory record for an active warehouse. */
    @Override
    @Transactional
    public InventoryResponse createInventory(InventoryRequest request) {
        Warehouse warehouse = findWarehouse(request.getWarehouseId());
        requireActiveWarehouse(warehouse);

        if (inventoryRepository.findByVariantIdAndWarehouseId(
                request.getVariantId(), request.getWarehouseId()).isPresent()) {
            throw new DuplicateInventoryException(
                    "Inventory already exists for variant " + request.getVariantId()
                            + " in warehouse " + request.getWarehouseId());
        }

        Inventory inventory = Inventory.builder()
                .variantId(request.getVariantId())
                .warehouseId(request.getWarehouseId())
                .quantity(request.getQuantity())
                .reservedQuantity(0)
                .build();

        return toResponse(inventoryRepository.save(inventory));
    }

    /** Returns stock state for one product variant in one warehouse. */
    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getInventory(Long variantId, Long warehouseId) {
        return toResponse(findInventory(variantId, warehouseId));
    }

    /** Returns all warehouse inventory records for a product variant. */
    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getInventoryByVariant(Long variantId) {
        return inventoryRepository.findByVariantId(variantId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /** Returns all inventory records belonging to a warehouse. */
    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getInventoryByWarehouse(Long warehouseId) {
        findWarehouse(warehouseId);
        return inventoryRepository.findByWarehouseId(warehouseId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /** Directly adjusts total physical quantity without changing reservations. */
    @Override
    @Transactional
    public InventoryResponse updateInventory(Long id, InventoryUpdateRequest request) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new InventoryNotFoundException(
                        "Inventory not found: " + id));

        if (request.getQuantity() < inventory.getReservedQuantity()) {
            throw new InvalidInventoryOperationException(
                    "Quantity cannot be less than reserved quantity");
        }

        inventory.setQuantity(request.getQuantity());
        return toResponse(inventoryRepository.save(inventory));
    }

    /** Deletes inventory only when no stock is currently reserved. */
    @Override
    @Transactional
    public void deleteInventory(Long id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new InventoryNotFoundException(
                        "Inventory not found: " + id));

        if (inventory.getReservedQuantity() > 0) {
            throw new InvalidInventoryOperationException(
                    "Cannot delete inventory while stock is reserved");
        }

        inventoryRepository.delete(inventory);
    }

    /** Increases physical stock at an active warehouse. */
    @Override
    @Transactional
    public void addStock(Long variantId, Long warehouseId, Integer quantity) {
        validateQuantity(quantity);
        requireActiveWarehouse(findWarehouse(warehouseId));

        Inventory inventory = findInventory(variantId, warehouseId);
        inventory.setQuantity(inventory.getQuantity() + quantity);
        inventoryRepository.save(inventory);
    }

    /** Decreases physical stock only when enough unreserved stock is available. */
    @Override
    @Transactional
    public void reduceStock(Long variantId, Long warehouseId, Integer quantity) {
        validateQuantity(quantity);
        requireActiveWarehouse(findWarehouse(warehouseId));

        Inventory inventory = findInventory(variantId, warehouseId);

        if (inventory.getAvailableQuantity() < quantity) {
            throw new InsufficientStockException(
                    "Insufficient available stock for variant " + variantId
                            + " in warehouse " + warehouseId);
        }

        inventory.setQuantity(inventory.getQuantity() - quantity);
        inventoryRepository.save(inventory);
    }

    /** Reserves available stock for an order or other pending business operation. */
    @Override
    @Transactional
    public void reserveStock(Long variantId, Long warehouseId, Integer quantity) {
        validateQuantity(quantity);
        requireActiveWarehouse(findWarehouse(warehouseId));

        Inventory inventory = findInventory(variantId, warehouseId);

        if (inventory.getAvailableQuantity() < quantity) {
            throw new InsufficientStockException(
                    "Insufficient available stock for reservation");
        }

        inventory.setReservedQuantity(
                inventory.getReservedQuantity() + quantity);
        inventoryRepository.save(inventory);
    }

    /** Releases previously reserved stock even when a warehouse has since been deactivated. */
    @Override
    @Transactional
    public void releaseStock(Long variantId, Long warehouseId, Integer quantity) {
        validateQuantity(quantity);

        Inventory inventory = findInventory(variantId, warehouseId);

        if (inventory.getReservedQuantity() < quantity) {
            throw new InvalidInventoryOperationException(
                    "Cannot release more stock than reserved");
        }

        inventory.setReservedQuantity(
                inventory.getReservedQuantity() - quantity);
        inventoryRepository.save(inventory);
    }

    /** Finds an inventory row or raises the service-specific not-found error. */
    private Inventory findInventory(Long variantId, Long warehouseId) {
        return inventoryRepository.findByVariantIdAndWarehouseId(variantId, warehouseId)
                .orElseThrow(() -> new InventoryNotFoundException(
                        "Inventory not found for variant " + variantId
                                + " and warehouse " + warehouseId));
    }

    /** Finds a warehouse or raises the service-specific not-found error. */
    private Warehouse findWarehouse(Long warehouseId) {
        return warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new WarehouseNotFoundException(
                        "Warehouse not found: " + warehouseId));
    }

    /** Prevents zero and negative quantities from entering stock operations. */
    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
    }

    /** Prevents operational stock changes against an inactive warehouse. */
    private void requireActiveWarehouse(Warehouse warehouse) {
        if (!Boolean.TRUE.equals(warehouse.getActive())) {
            throw new InactiveWarehouseException(
                    "Warehouse is inactive: " + warehouse.getCode());
        }
    }

    /** Converts an entity into the stable API representation with availability status. */
    private InventoryResponse toResponse(Inventory inventory) {
        int availableQuantity = inventory.getAvailableQuantity();
        String status = availableQuantity == 0
                ? "OUT_OF_STOCK"
                : availableQuantity <= LOW_STOCK_THRESHOLD
                ? "LOW_STOCK"
                : "IN_STOCK";

        return InventoryResponse.builder()
                .variantId(inventory.getVariantId())
                .warehouseId(inventory.getWarehouseId())
                .quantity(inventory.getQuantity())
                .reservedQuantity(inventory.getReservedQuantity())
                .availableQuantity(availableQuantity)
                .status(status)
                .build();
    }
}
