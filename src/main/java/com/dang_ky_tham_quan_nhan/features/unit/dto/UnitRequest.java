package com.dang_ky_tham_quan_nhan.features.unit.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request object for creating or updating a unit")
public class UnitRequest {
    
    @Schema(description = "Name of the unit", example = "Battalion 1")
    private String name;
    
    @Schema(description = "ID of the parent unit (if any)", example = "1")
    private Long parentId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
}