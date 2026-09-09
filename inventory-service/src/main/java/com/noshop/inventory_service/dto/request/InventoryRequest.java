package com.noshop.inventory_service.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryRequest {

    @NotNull
    private Long variantId;

    @NotNull
    private Long warehouseId;

    @NotNull
    @Min(0)
    private Integer quantity;
}