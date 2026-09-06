package com.noshop.product_service.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubCategoryResponse {

    private Long id;

    private String name;

    private String slug;

    private Integer displayOrder;

    private boolean active;

    private Long categoryId;

    private String categoryName;

}