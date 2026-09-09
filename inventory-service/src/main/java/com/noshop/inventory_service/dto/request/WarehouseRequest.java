package com.noshop.inventory_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseRequest {

    @NotBlank
    private String code;

    @NotBlank
    private String name;

    @NotBlank
    private String city;

    private String address;
}