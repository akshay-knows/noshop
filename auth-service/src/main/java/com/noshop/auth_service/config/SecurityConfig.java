package com.noshop.auth_service.config;

import com.noshop.auth_service.security.CustomUserDetailsService;
import com.noshop.auth_service.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration for auth-service.
 * <p>
 * Request flow:
 * Client → SecurityFilterChain → public? → yes: allow | no: JwtAuthenticationFilter
 * validates the Bearer token → SecurityContext populated → request proceeds.
 * <p>
 * Login flow (separate from the filter chain above): AuthenticationManager delegates
 * to AuthenticationProvider (DaoAuthenticationProvider), which loads the user via
 * CustomUserDetailsService → UserRepository → Database, then verifies the password
 * with PasswordEncoder.matches(). This is only used inside AuthServiceImpl.login() —
 * not for validating requests to already-protected endpoints, which JwtAuthenticationFilter
 * handles instead.
 * <p>
 * Stateless session policy is set since auth is entirely token-based — no server-side
 * session state is created or relied on between requests.
 */
@SuppressWarnings({"deprecation", "Convert2MethodRef"})
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * Verifies raw passwords against the encoded password stored in the DB.
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * Loads a User by email for Spring Security during authentication.
     */
    private final CustomUserDetailsService customUserDetailsService;

    /**
     * Validates incoming JWTs on every request and populates the SecurityContext.
     */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth.requestMatchers(
                                                       "/api/v1/auth/**",
                                                       "/actuator/health/**",
                                                       "/actuator/info"
                                               )
                                               .permitAll()
                                               .anyRequest()
                                               .authenticated())

            .authenticationProvider(authenticationProvider())

            // Run before Spring's default filter so the JWT (if present) sets
            // authentication before anything else evaluates the request.
            .addFilterBefore(
                    jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }

    /**
     * Wires our UserDetailsService + PasswordEncoder into Spring's DB-backed auth provider.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    /**
     * Exposes Spring's AuthenticationManager so it can be injected into the login() flow later.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}