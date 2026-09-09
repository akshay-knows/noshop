package com.noshop.inventory_service.util;

import com.noshop.inventory_service.entity.Warehouse;
import com.noshop.inventory_service.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WarehouseDataLoader implements CommandLineRunner {

    private final WarehouseRepository warehouseRepository;

    @Override
    public void run(String... args) {
        createIfMissing("BLR-01", "Bengaluru Central Warehouse", "Bengaluru", "Peenya Industrial Area");
        createIfMissing("BLR-02", "Bengaluru East Warehouse", "Bengaluru", "Whitefield");
        createIfMissing("MYS-01", "Mysuru Warehouse", "Mysuru", "Hebbal Industrial Area");
    }

    private void createIfMissing(String code, String name, String city, String address) {
        if (warehouseRepository.existsByCode(code)) {
            return;
        }

        warehouseRepository.save(Warehouse.builder()
                .code(code)
                .name(name)
                .city(city)
                .address(address)
                .active(true)
                .build());
    }
}
