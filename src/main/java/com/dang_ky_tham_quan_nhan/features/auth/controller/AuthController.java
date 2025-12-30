package com.dang_ky_tham_quan_nhan.features.auth.controller;

import com.dang_ky_tham_quan_nhan.features.auth.dto.LoginRequest;
import com.dang_ky_tham_quan_nhan.features.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "Authentication APIs")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "User Login", description = "Authenticates a user and returns a token or session details.")
    public Map<String, Object> login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
}