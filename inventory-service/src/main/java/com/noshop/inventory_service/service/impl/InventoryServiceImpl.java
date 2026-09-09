package com.noshop.inventory_service.service.impl;

import com.noshop.inventory_service.dto.request.InventoryRequest;
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

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private static final int LOW_STOCK_THRESHOLD = 5;

    private final InventoryRepository inventoryRepository;
    private final WarehouseRepository warehouseRepository;

    @Override
    @Transactional
    public InventoryResponse createInventory(InventoryRequest request) {

        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() ->
                        new WarehouseNotFoundException(
                                "Warehouse not found: " + request.getWarehouseId()
                        )
                );

        if (!warehouse.getActive()) {
            throw new InactiveWarehouseException(
                    "Warehouse is inactive: " + warehouse.getCode()
            );
        }

        inventoryRepository
                .findByVariantIdAndWarehouseId(
                        request.getVariantId(),
                        request.getWarehouseId()
                )
                .ifPresent(existing -> {
                    throw new DuplicateInventoryException(
                            "Inventory already exists for variant "
                                    + request.getVariantId()
                                    + " in warehouse "
                                    + request.getWarehouseId()
                    );
                });

        Inventory inventory = Inventory.builder()
                .variantId(request.getVariantId())
                .warehouseId(request.getWarehouseId())
                .quantity(request.getQuantity())
                .reservedQuantity(0)
                .build();

        return toResponse(inventoryRepository.save(inventory));
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getInventory(Long variantId, Long warehouseId) {

        Inventory inventory = inventoryRepository
                .findByVariantIdAndWarehouseId(variantId, warehouseId)
                .orElseThrow(() ->
                        new InventoryNotFoundException(
                                "Inventory not found for variant "
                                        + variantId
                                        + " and warehouse "
                                        + warehouseId
                        )
                );

        return toResponse(inventory);
    }

    @Override
    @Transactional
    public void addStock(Long variantId, Long warehouseId, Integer quantity) {
        validateQuantity(quantity);

        Inventory inventory = findInventory(variantId, warehouseId);

        inventory.setQuantity(inventory.getQuantity() + quantity);
        inventoryRepository.save(inventory);
    }

    @Override
    @Transactional
    public void reduceStock(Long variantId, Long warehouseId, Integer quantity) {
        validateQuantity(quantity);

        Inventory inventory = findInventory(variantId, warehouseId);

        if (inventory.getAvailableQuantity() < quantity) {
            throw new InsufficientStockException(
                    "Insufficient available stock for variant "
                            + variantId
                            + " in warehouse "
                            + warehouseId
            );
        }

        inventory.setQuantity(inventory.getQuantity() - quantity);
        inventoryRepository.save(inventory);
    }

    @Override
    @Transactional
    public void reserveStock(Long variantId, Long warehouseId, Integer quantity) {
        validateQuantity(quantity);

        Inventory inventory = findInventory(variantId, warehouseId);

        if (inventory.getAvailableQuantity() < quantity) {
            throw new InsufficientStockException(
                    "Insufficient available stock for reservation"
            );
        }

        inventory.setReservedQuantity(
                inventory.getReservedQuantity() + quantity
        );

        inventoryRepository.save(inventory);
    }

    @Override
    @Transactional
    public void releaseStock(Long variantId, Long warehouseId, Integer quantity) {
        validateQuantity(quantity);

        Inventory inventory = findInventory(variantId, warehouseId);

        if (inventory.getReservedQuantity() < quantity) {
            throw new InvalidInventoryOperationException(
                    "Cannot release more stock than reserved"
            );
        }

        inventory.setReservedQuantity(
                inventory.getReservedQuantity() - quantity
        );

        inventoryRepository.save(inventory);
    }

    private Inventory findInventory(Long variantId, Long warehouseId) {
        return inventoryRepository
                .findByVariantIdAndWarehouseId(variantId, warehouseId)
                .orElseThrow(() ->
                        new InventoryNotFoundException(
                                "Inventory not found for variant "
                                        + variantId
                                        + " and warehouse "
                                        + warehouseId
                        )
                );
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }
    }

    private InventoryResponse toResponse(Inventory inventory) {

        int availableQuantity = inventory.getAvailableQuantity();
        String status;

        if (availableQuantity == 0) {
            status = "OUT_OF_STOCK";
        } else if (availableQuantity <= LOW_STOCK_THRESHOLD) {
            status = "LOW_STOCK";
        } else {
            status = "IN_STOCK";
        }

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
