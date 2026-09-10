package com.noshop.inventory_service.util;

import com.noshop.inventory_service.entity.Inventory;
import com.noshop.inventory_service.entity.Warehouse;
import com.noshop.inventory_service.repository.InventoryRepository;
import com.noshop.inventory_service.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Seeds repeatable inventory rows for local development and load testing. */
@Component
@Order(20)
@RequiredArgsConstructor
public class ProductVariantInventorySeeder implements CommandLineRunner {

    private final InventoryRepository inventoryRepository;
    private final WarehouseRepository warehouseRepository;

    @Value("${noshop.seed.enabled:false}")
    private boolean seedEnabled;

    @Value("${noshop.seed.inventory-variant-count:1000}")
    private int variantCount;

    @Override
    public void run(String... args) {
        if (!seedEnabled || variantCount <= 0) {
            return;
        }

        List<Warehouse> warehouses = warehouseRepository.findAll().stream()
                .filter(warehouse -> Boolean.TRUE.equals(warehouse.getActive()))
                .toList();

        if (warehouses.isEmpty()) {
            return;
        }

        List<Inventory> pending = new ArrayList<>();
        for (int variantId = 1; variantId <= variantCount; variantId++) {
            for (Warehouse warehouse : warehouses) {
                if (inventoryRepository.findByVariantIdAndWarehouseId(
                        (long) variantId,
                        warehouse.getId()).isEmpty()) {

                    int quantity = 20 + (variantId % 80);
                    int reserved = variantId % 5;

                    pending.add(Inventory.builder()
                            .variantId((long) variantId)
                            .warehouseId(warehouse.getId())
                            .quantity(quantity)
                            .reservedQuantity(reserved)
                            .build());
                }
            }
        }

        if (!pending.isEmpty()) {
            inventoryRepository.saveAll(pending);
        }
    }
}
