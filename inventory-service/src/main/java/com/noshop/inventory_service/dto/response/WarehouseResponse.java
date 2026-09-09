package com.noshop.inventory_service.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseResponse {

    private Long id;

    private String code;

    private String name;

    private String city;

    private String address;

    private Boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}