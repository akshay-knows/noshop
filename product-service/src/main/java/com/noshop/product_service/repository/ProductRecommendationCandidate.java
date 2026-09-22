package com.noshop.product_service.repository;

import java.math.BigDecimal;

/**
 * Lightweight projection used by the recommendation query so the service
 * does not materialize complete product graphs for every candidate.
 */
public interface ProductRecommendationCandidate {

    Long getId();

    String getName();

    String getSlug();

    BigDecimal getPrice();
}
