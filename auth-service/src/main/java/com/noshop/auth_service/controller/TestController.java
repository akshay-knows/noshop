package com.noshop.auth_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/v1/auth/test")
    public String test() {
        return "JWT Authentication Successful";
    }
}