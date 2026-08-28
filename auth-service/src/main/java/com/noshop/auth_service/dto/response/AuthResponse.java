package com.noshop.auth_service.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {

    private Long id;

    private String firstName;

    private String lastName;

    private String email;

    private String message;

    private String token;
}
