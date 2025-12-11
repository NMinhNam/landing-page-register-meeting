package com.dang_ky_tham_quan_nhan.features.visit.dto;

import com.dang_ky_tham_quan_nhan.common.enums.VisitStatus;

public class UpdateStatusRequest {
    private VisitStatus status; // APPROVED, REJECTED
    private String note;

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
