package com.noshop.product_service.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SizeResponse {

    private Long id;

    private String name;

    private Integer displayOrder;

    private boolean active;

}