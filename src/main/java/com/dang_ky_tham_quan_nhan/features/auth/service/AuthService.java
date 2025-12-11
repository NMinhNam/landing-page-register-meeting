package com.dang_ky_tham_quan_nhan.features.auth.service;

import com.dang_ky_tham_quan_nhan.features.auth.dto.LoginRequest;
import com.dang_ky_tham_quan_nhan.features.auth.entity.AdminUser;
import com.dang_ky_tham_quan_nhan.features.auth.mapper.AdminUserMapper;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

    private final AdminUserMapper adminUserMapper;

    public AuthService(AdminUserMapper adminUserMapper) {
        this.adminUserMapper = adminUserMapper;
    }

    public Map<String, Object> login(LoginRequest request) {
        AdminUser user = adminUserMapper.findByUsername(request.getUsername());
        
        if (user != null && user.getPassword().equals(request.getPassword())) {
            Map<String, Object> response = new HashMap<>();
            response.put("token", UUID.randomUUID().toString());
            response.put("role", user.getRole());
            response.put("username", user.getUsername());
            return response;
        }
        throw new RuntimeException("Invalid credentials");
    }
}