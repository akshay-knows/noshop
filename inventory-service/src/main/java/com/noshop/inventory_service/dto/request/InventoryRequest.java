package com.noshop.inventory_service.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryRequest {

    @NotNull
    @Positive
    private Long variantId;

    @NotNull
    @Positive
    private Long warehouseId;

    @NotNull
    @Min(1)
    private Integer quantity;
}