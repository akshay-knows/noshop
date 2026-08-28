package com.noshop.auth_service.service;

import com.noshop.auth_service.dto.request.LoginRequest;
import com.noshop.auth_service.dto.request.RegisterRequest;
import com.noshop.auth_service.dto.response.AuthResponse;
import com.noshop.auth_service.entity.Role;
import com.noshop.auth_service.entity.User;
import com.noshop.auth_service.repository.UserRepository;
import com.noshop.auth_service.security.CustomUserDetails;
import com.noshop.auth_service.security.JwtService;
import com.noshop.common.exception.EmailAlreadyExistsException;
import com.noshop.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Implements registration and login for auth-service.
 * <p>
 * register(): validates the email is unused, encodes the password with BCrypt,
 * defaults the new user to ROLE_USER, and persists them.
 * <p>
 * login(): delegates credential verification to Spring Security's AuthenticationManager
 * (which internally uses DaoAuthenticationProvider → CustomUserDetailsService →
 * PasswordEncoder.matches()). If authenticate() doesn't throw, the credentials are valid,
 * so the user is re-fetched by email, wrapped in CustomUserDetails, and used to generate
 * a signed JWT via JwtService — that token is what the client uses to stay authenticated
 * on subsequent requests (validated per-request by JwtAuthenticationFilter).
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(encodedPassword);
        user.setEnabled(true);
        user.setRole(Role.ROLE_USER);
        User savedUser = userRepository.save(user);

        return AuthResponse.builder()
                           .id(savedUser.getId())
                           .firstName(savedUser.getFirstName())
                           .lastName(savedUser.getLastName())
                           .email(savedUser.getEmail())
                           .message("User registered successfully")
                           .build();
    }

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(),
                loginRequest.getPassword()
        ));

        User user = userRepository.findByEmail(loginRequest.getEmail())
                                  .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Wrap the entity so JwtService can read the fields it needs via UserDetails.
        UserDetails userDetails = new CustomUserDetails(user);

        String jwtToken = jwtService.generateToken(userDetails);

        return AuthResponse.builder()
                           .id(user.getId())
                           .firstName(user.getFirstName())
                           .lastName(user.getLastName())
                           .email(user.getEmail())
                           .token(jwtToken)
                           .message("Login successful. Welcome " + user.getFirstName() + "!")
                           .build();
    }
}