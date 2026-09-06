package com.noshop.product_service.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateProductVariantRequest {

    @NotBlank
    private String sku;

    @NotBlank
    private String color;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal mrp;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal sellingPrice;

    @NotNull
    private Long productId;

    @NotNull
    private Long sizeId;
}