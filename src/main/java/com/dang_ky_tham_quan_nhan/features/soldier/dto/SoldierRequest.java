package com.dang_ky_tham_quan_nhan.features.soldier.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request object for creating or updating a soldier")
public class SoldierRequest {
    
    @Schema(description = "Unique code of the soldier", example = "SOL001")
    private String code;
    
    @Schema(description = "Full name of the soldier", example = "Nguyen Van A")
    private String name;
    
    @Schema(description = "ID of the unit the soldier belongs to", example = "1")
    private Long unitId;
    
    @Schema(description = "Current status of the soldier", example = "ACTIVE")
    private String status;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getUnitId() {
        return unitId;
    }

    public void setUnitId(Long unitId) {
        this.unitId = unitId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}