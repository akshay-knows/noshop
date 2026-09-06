package com.noshop.product_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCategoryRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String slug;

    private Integer displayOrder;


}