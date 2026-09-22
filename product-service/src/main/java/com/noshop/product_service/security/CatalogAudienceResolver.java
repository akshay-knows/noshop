package com.noshop.product_service.security;

import com.noshop.product_service.enums.CatalogAudience;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;

/**
 * Converts the authenticated Spring Security role into the catalog audience
 * that can be exposed to the current request.
 *
 * ROLE_USER -> B2C
 * ROLE_B2B  -> B2B
 * ROLE_ADMIN -> both customer segments
 * unauthenticated -> B2C
 */
@Component
public class CatalogAudienceResolver {

    public CatalogAudience resolveCurrentAudience() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getAuthorities().isEmpty()) {
            return CatalogAudience.B2C;
        }

        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

        if (admin) {
            return CatalogAudience.BOTH;
        }

        boolean b2b = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_B2B".equals(a.getAuthority()));

        return b2b ? CatalogAudience.B2B : CatalogAudience.B2C;
    }

    public Set<CatalogAudience> allowedAudiences() {
        return switch (resolveCurrentAudience()) {
            case B2B -> Set.of(CatalogAudience.B2B, CatalogAudience.BOTH);
            case B2C -> Set.of(CatalogAudience.B2C, CatalogAudience.BOTH);
            case BOTH -> EnumSet.allOf(CatalogAudience.class);
        };
    }

    public boolean isVisible(CatalogAudience productAudience) {
        return productAudience == null
                || productAudience == CatalogAudience.BOTH
                || allowedAudiences().contains(productAudience);
    }
}
