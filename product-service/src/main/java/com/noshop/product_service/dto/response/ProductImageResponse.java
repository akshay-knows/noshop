package com.noshop.product_service.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductImageResponse {

    private Long id;
    private String imageUrl;
    private Long productId;
    private String productName;

}