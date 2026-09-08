package com.noshop.product_service.dto.request;

import com.noshop.product_service.enums.ProductStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateProductStatusRequest {

    @NotNull(message = "Product status is required")
    private ProductStatus status;
}