package com.noshop.product_service.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProductResponse {

    private Long id;

    private String name;

    private String slug;

    private String description;

    private boolean active;

    private Long brandId;

    private String brandName;

    private Long categoryId;

    private String categoryName;

    private Long subCategoryId;

    private String subCategoryName;

    private List<ProductImageResponse> images;

}