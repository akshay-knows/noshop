package com.noshop.product_service.config;

import com.noshop.product_service.enums.CatalogAudience;
import com.noshop.product_service.security.CatalogAudienceResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Separates Redis product-detail entries for B2C, B2B and admin requests.
 */
@Component("productCacheKeyGenerator")
@RequiredArgsConstructor
public class ProductCacheKeyGenerator implements KeyGenerator {

    private final CatalogAudienceResolver audienceResolver;

    @Override
    public Object generate(Object target, Method method, Object... params) {
        Long productId = (Long) params[0];
        CatalogAudience audience = audienceResolver.resolveCurrentAudience();
        return productId + ":" + audience.name();
    }
}
