package com.dang_ky_tham_quan_nhan.features.auth.controller;

import com.dang_ky_tham_quan_nhan.features.auth.dto.LoginRequest;
import com.dang_ky_tham_quan_nhan.features.auth.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
