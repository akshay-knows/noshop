package com.noshop.product_service.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductVariantResponse {

    private Long id;

    private String sku;

    private BigDecimal packSize;

    private String unit;

    private BigDecimal price;

    private String status;

    private Long productId;

    private String productName;
}