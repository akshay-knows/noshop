package com.noshop.product_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantEvent {

    private Long variantId;
    private String sku;
    private BigDecimal packSize;
    private String unit;
    private BigDecimal price;
}
