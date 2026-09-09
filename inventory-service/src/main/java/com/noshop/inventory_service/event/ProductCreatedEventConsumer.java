package com.noshop.inventory_service.event;

import com.noshop.inventory_service.entity.Inventory;
import com.noshop.inventory_service.entity.Warehouse;
import com.noshop.inventory_service.repository.InventoryRepository;
import com.noshop.inventory_service.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductCreatedEventConsumer {

    private final InventoryRepository inventoryRepository;
    private final WarehouseRepository warehouseRepository;

    @KafkaListener(
            topics = "product-created",
            groupId = "inventory-service",
            containerFactory = "productEventKafkaListenerContainerFactory"
    )
    @Transactional
    public void handleProductCreated(ProductCreatedEvent event) {
        if (event == null || event.getVariants() == null) {
            log.warn("Ignoring product-created event without variants");
            return;
        }

        var activeWarehouses = warehouseRepository.findAll().stream()
                .filter(warehouse -> Boolean.TRUE.equals(warehouse.getActive()))
                .toList();

        for (ProductVariantEvent variant : event.getVariants()) {
            for (Warehouse warehouse : activeWarehouses) {
                boolean exists = inventoryRepository
                        .findByVariantIdAndWarehouseId(variant.getVariantId(), warehouse.getId())
                        .isPresent();

                if (!exists) {
                    inventoryRepository.save(Inventory.builder()
                            .variantId(variant.getVariantId())
                            .warehouseId(warehouse.getId())
                            .quantity(0)
                            .reservedQuantity(0)
                            .build());
                }
            }
        }

        log.info("Initialized inventory for product {} across {} active warehouses",
                event.getProductId(), activeWarehouses.size());
    }
}
