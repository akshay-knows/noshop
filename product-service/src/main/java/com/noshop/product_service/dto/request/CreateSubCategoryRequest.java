package com.noshop.product_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateSubCategoryRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String slug;

    @NotNull
    private Integer displayOrder;

    @NotNull
    private Long categoryId;

}