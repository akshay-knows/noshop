package com.noshop.auth_service.service;


import com.noshop.auth_service.dto.request.LoginRequest;
import com.noshop.auth_service.dto.request.RegisterRequest;
import com.noshop.auth_service.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest registerRequest);
    AuthResponse login(LoginRequest loginRequest);

}
