package com.noshop.auth_service.util;

import com.noshop.auth_service.entity.Role;
import com.noshop.auth_service.entity.User;
import com.noshop.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Creates a local-only B2B demo account when explicitly enabled.
 * The normal registration flow remains ROLE_USER/B2C.
 */
@Component
@Order(5)
@RequiredArgsConstructor
public class B2bDemoUserSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("\${noshop.seed.b2b-user.enabled:false}")
    private boolean enabled;

    @Value("\${noshop.seed.b2b-user.email:b2b.demo@noshop.local}")
    private String email;

    @Value("\${noshop.seed.b2b-user.password:ChangeMe123!}")
    private String password;

    @Override
    public void run(String... args) {
        if (!enabled || userRepository.findByEmail(email).isPresent()) {
            return;
        }

        User user = User.builder()
                .firstName("B2B")
                .lastName("Demo")
                .email(email.trim().toLowerCase())
                .password(passwordEncoder.encode(password))
                .enabled(true)
                .role(Role.ROLE_B2B)
                .build();

        userRepository.save(user);
    }
}
