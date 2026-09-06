package com.noshop.product_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateBrandRequest {

    @NotBlank
    private String name;

    private String description;

    private String logoUrl;

}