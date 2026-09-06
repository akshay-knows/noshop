package com.noshop.product_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateProductRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String slug;

    private String description;

    @NotNull
    private Long brandId;

    @NotNull
    private Long categoryId;

    @NotNull
    private Long subCategoryId;
}