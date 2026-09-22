package com.noshop.product_service.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductRecommendationResponse {

    private Long productId;

    private String name;

    private String slug;

    private BigDecimal price;

    private BigDecimal priceDifference;
}
