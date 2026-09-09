package com.noshop.inventory_service.repository;

import com.noshop.inventory_service.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByVariantIdAndWarehouseId(
            Long variantId,
            Long warehouseId
    );

    List<Inventory> findByVariantId(Long variantId);

    List<Inventory> findByWarehouseId(Long warehouseId);
}