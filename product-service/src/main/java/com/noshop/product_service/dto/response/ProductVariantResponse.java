package com.noshop.product_service.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductVariantResponse {

    private Long id;

    private String sku;

    private String color;

    private BigDecimal mrp;

    private BigDecimal sellingPrice;

    private boolean active;

    private Long productId;

    private String productName;

    private Long sizeId;

    private String sizeName;
}