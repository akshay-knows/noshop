package com.noshop.auth_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Runs once per request to authenticate callers based on a JWT, instead of
 * session cookies or Basic auth.
 * <p>
 * Flow: reads the {@code Authorization: Bearer <token>} header → extracts the
 * username (email) from the token → loads that user via CustomUserDetailsService →
 * validates the token against that user (signature already verified by
 * JwtService, plus expiry + subject match) → if valid, manually populates the
 * SecurityContext so the rest of the request is treated as authenticated.
 * <p>
 * Requests with no/invalid Bearer header are simply passed through unauthenticated —
 * SecurityConfig's {@code anyRequest().authenticated()} rule is what actually
 * rejects them later if the endpoint requires auth. Requests to permitAll() routes
 * (like /api/v1/auth/**) pass through fine either way.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** Service responsible for JWT operations (parsing, validating, extracting claims). */
    private final JwtService jwtService;

    /** Loads user details from the database. */
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        System.out.println("jwt Filter");

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        System.out.println(authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        final String userEmail = jwtService.extractUsername(jwt);
        System.out.println(userEmail);

        // Authenticate only if not already authenticated in this request.
        if (userEmail != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails =
                    customUserDetailsService.loadUserByUsername(userEmail);
            System.out.println(userDetails);

            boolean valid = jwtService.isTokenValid(jwt, userDetails);

            System.out.println("Token Valid = " + valid);
            // Validate signature owner + expiry before trusting the token.
            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                SecurityContextHolder.getContext().setAuthentication(authToken);
                System.out.println(SecurityContextHolder.getContext().getAuthentication());
            }
        }

        filterChain.doFilter(request, response);
    }
}