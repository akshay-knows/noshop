package com.noshop.product_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/** Parses and validates JWTs issued by auth-service for product-service requests. */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey secretKey;

    /** Builds the HMAC verification key once at startup. */
    @PostConstruct
    public void init() {
        secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /** Extracts the authenticated user's email from the token subject. */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /** Extracts role authorities embedded by auth-service. */
    public List<String> extractRoles(String token) {
        Object value = extractAllClaims(token).get("roles");

        if (value instanceof List<?> roles) {
            return roles.stream().map(String::valueOf).toList();
        }

        if (value instanceof String role) {
            return List.of(role);
        }

        return List.of();
    }

    /** Returns true when the JWT signature and expiration are valid. */
    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.getSubject() != null
                    && claims.getExpiration() != null
                    && !claims.getExpiration().before(new Date());
        } catch (Exception ex) {
            return false;
        }
    }

    /** Parses and verifies the signed JWT payload. */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
