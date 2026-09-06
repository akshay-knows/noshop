package com.noshop.product_service.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BrandResponse {

    private Long id;

    private String name;

    private String description;

    private String logoUrl;

    private boolean active;

}