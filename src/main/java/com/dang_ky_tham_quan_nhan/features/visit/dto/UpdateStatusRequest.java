package com.dang_ky_tham_quan_nhan.features.visit.dto;

import com.dang_ky_tham_quan_nhan.common.enums.VisitStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request object for updating visit status")
public class UpdateStatusRequest {
    
    @Schema(description = "New status for the visit (APPROVED, REJECTED)", example = "APPROVED")
    private VisitStatus status;
    
    @Schema(description = "Note or reason for the status change", example = "Approved for visit.")
    private String note;
    
    private Long adminId;

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

    public VisitStatus getStatus() {
        return status;
    }

    public void setStatus(VisitStatus status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}