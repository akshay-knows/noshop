package com.noshop.product_service.security;

import com.noshop.product_service.enums.CatalogAudience;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatalogAudienceResolverTest {

    private final CatalogAudienceResolver resolver = new CatalogAudienceResolver();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void defaultsToB2CWhenUnauthenticated() {
        assertEquals(CatalogAudience.B2C, resolver.resolveCurrentAudience());
        assertEquals(
                Set.of(CatalogAudience.B2C, CatalogAudience.BOTH),
                resolver.allowedAudiences());
    }

    @Test
    void mapsB2bRoleToB2bAudience() {
        authenticate("ROLE_B2B");

        assertEquals(CatalogAudience.B2B, resolver.resolveCurrentAudience());
        assertEquals(
                Set.of(CatalogAudience.B2B, CatalogAudience.BOTH),
                resolver.allowedAudiences());
    }

    @Test
    void mapsAdminToBothCustomerSegments() {
        authenticate("ROLE_ADMIN");

        assertEquals(CatalogAudience.BOTH, resolver.resolveCurrentAudience());
        assertTrue(resolver.allowedAudiences().contains(CatalogAudience.B2B));
        assertTrue(resolver.allowedAudiences().contains(CatalogAudience.B2C));
    }

    @Test
    void bothAudienceIsVisibleToEverySegment() {
        authenticate("ROLE_B2B");

        assertTrue(resolver.isVisible(CatalogAudience.BOTH));
        assertTrue(resolver.isVisible(CatalogAudience.B2B));
    }

    @Test
    void b2cCannotSeeB2bOnlyProduct() {
        authenticate("ROLE_USER");

        assertTrue(resolver.isVisible(CatalogAudience.B2C));
        assertTrue(!resolver.isVisible(CatalogAudience.B2B));
    }

    private void authenticate(String authority) {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken(
                        "test-user",
                        null,
                        new SimpleGrantedAuthority(authority)));
    }
}
