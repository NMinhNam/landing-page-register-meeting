package com.dang_ky_tham_quan_nhan.features.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request object for user login")
public class LoginRequest {
    
    @Schema(description = "Username for authentication", example = "admin")
    private String username;
    
    @Schema(description = "Password for authentication", example = "password123")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}