package com.noshop.inventory_service.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryResponse {

    private Long variantId;

    private Long warehouseId;

    private Integer quantity;

    private Integer reservedQuantity;

    private Integer availableQuantity;

    private String status;
}