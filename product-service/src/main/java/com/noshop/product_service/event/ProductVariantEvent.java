package com.noshop.product_service.event;

import com.noshop.product_service.enums.Unit;
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
    private Unit unit;
    private BigDecimal price;
}
