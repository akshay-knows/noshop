package com.noshop.auth_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/** Creates signed JWTs containing the user's identity and roles. */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private SecretKey secretKey;

    /** Builds the HMAC signing key once when the service starts. */
    @PostConstruct
    public void init() {
        secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /** Creates a signed JWT containing the username and granted roles. */
    public String generateToken(UserDetails userDetails) {
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(secretKey)
                .compact();
    }

    /** Parses and verifies a signed JWT and returns its claims. */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** Extracts the authenticated user's email from the token subject. */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /** Extracts the role authorities embedded in the token. */
    public List<String> extractRoles(String token) {
        Object value = extractAllClaims(token).get("roles");

        if (value instanceof List<?> roles) {
            return roles.stream()
                    .map(String::valueOf)
                    .toList();
        }

        if (value instanceof String role) {
            return List.of(role);
        }

        return List.of();
    }

    /** Returns true when the token is structurally valid and has not expired. */
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
}
