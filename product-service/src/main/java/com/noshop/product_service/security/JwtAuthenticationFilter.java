package com.noshop.product_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        System.out.println("========== JWT FILTER ==========");
        System.out.println(request.getMethod() + " " + request.getRequestURI());

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        System.out.println("Authorization Header : " + authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("No Bearer Token");
            filterChain.doFilter(request, response);
            return;
        }

        try {

            String jwt = authHeader.substring(7);

            System.out.println("JWT : " + jwt);

            String email = jwtService.extractUserName(jwt);

            System.out.println("Email : " + email);

            if (email != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                JwtUser jwtUser = new JwtUser(
                        email,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                );

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                jwtUser,
                                null,
                                jwtUser.getAuthorities()
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);

                System.out.println("Authentication Successful");
            }

        } catch (Exception e) {

            System.out.println("JWT ERROR");
            e.printStackTrace();

            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}