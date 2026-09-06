package com.noshop.product_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateSizeRequest {

    @NotBlank
    private String name;

    private Integer displayOrder;

}