package com.noshop.auth_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Issues and validates JWTs for authenticated users.
 * <p>
 * generateToken(): builds a signed JWT with the user's email (username) as the
 * subject, an issued-at timestamp, and an expiry based on {@code jwt.expiration}.
 * <p>
 * Validation flow: extractAllClaims() parses + verifies the token's signature in
 * one step (parseSignedClaims throws if the signature or structure is invalid) —
 * extractUsername()/extractExpiration() then read individual claims from that
 * payload, and isTokenValid() combines both checks (username match + not expired)
 * into the single check callers actually need.
 * <p>
 * The signing key is built once in init() from the raw secret string, rather than
 * re-deriving it from {@code secret} on every call.
 */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private SecretKey secretKey;

    /** Derives the HMAC signing key once at startup from the configured secret. */
    @PostConstruct
    public void init() {
        secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    }

    /** Builds a signed JWT for the given user, encoding their email as the subject. */
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                   .subject(userDetails.getUsername())                 // sub claim
                   .issuedAt(new Date())                               // iat claim
                   .expiration(new Date(System.currentTimeMillis() + jwtExpiration)) // exp claim
                   .signWith(secretKey)                                // Digital Signature
                   .compact();                                         // Convert to JWT String
    }

    /** Parses and verifies the token's signature, returning its claims payload. */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                   .verifyWith(secretKey)
                   .build()
                   .parseSignedClaims(token)
                   .getPayload();
    }

    /** Reads the subject (email) out of a valid token. */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /** Reads the expiration timestamp out of a valid token. */
    private Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    /** Checks whether the token's expiry has already passed. */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /** Combines identity check + expiry check into the single validation callers need. */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        // Confirm the token actually belongs to this user, and hasn't expired.
        String username = extractUsername(token);

        return username.equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }
}