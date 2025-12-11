package com.dang_ky_tham_quan_nhan.features.auth.entity;

import java.io.Serializable;

public class AdminUser implements Serializable {
    private Long id;
    private String username;
    private String password;
    private Long unitId;
    private String role; // ADMIN, VIEWER

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Long getUnitId() {
        return unitId;
    }

    public void setUnitId(Long unitId) {
        this.unitId = unitId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}