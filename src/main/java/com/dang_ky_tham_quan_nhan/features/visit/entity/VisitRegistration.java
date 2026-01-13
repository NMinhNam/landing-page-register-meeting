package com.dang_ky_tham_quan_nhan.features.visit.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

public class VisitRegistration implements Serializable {
    private Long id;
    private Long soldierId;
    private Long unitId;
    private String manualSoldierName;
    private String manualUnitName;
    private String representativePhone;
    private String province;
    private Integer visitWeek;
    private String visitWeekMonthDisplay; // Calculated display format: week/month (e.g., 1/03)
    private Integer visitYear; // Year of the visit registration
    private Integer visitMonth; // Month of the visit registration (based on display logic)
    private String status; // PENDING, APPROVED, REJECTED
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;

    // Transient field for details
    private java.util.List<Relative> relatives;

    public Long getId() {
        return id;
    }
    
    public void setManualSoldierName(String manualSoldierName) {
        this.manualSoldierName = manualSoldierName;
    }

    public String getManualSoldierName() {
        return manualSoldierName;
    }

    public void setManualUnitName(String manualUnitName) {
        this.manualUnitName = manualUnitName;
    }

    public String getManualUnitName() {
        return manualUnitName;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSoldierId() {
        return soldierId;
    }

    public void setSoldierId(Long soldierId) {
        this.soldierId = soldierId;
    }

    public Long getUnitId() {
        return unitId;
    }

    public void setUnitId(Long unitId) {
        this.unitId = unitId;
    }

    public String getRepresentativePhone() {
        return representativePhone;
    }

    public void setRepresentativePhone(String representativePhone) {
        this.representativePhone = representativePhone;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public Integer getVisitWeek() {
        return visitWeek;
    }

    public void setVisitWeek(Integer visitWeek) {
        this.visitWeek = visitWeek;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public java.util.List<Relative> getRelatives() {
        return relatives;
    }

    public void setRelatives(java.util.List<Relative> relatives) {
        this.relatives = relatives;
    }

    public String getVisitWeekMonthDisplay() {
        return visitWeekMonthDisplay;
    }

    public void setVisitWeekMonthDisplay(String visitWeekMonthDisplay) {
        this.visitWeekMonthDisplay = visitWeekMonthDisplay;
    }

    public Integer getVisitYear() {
        return visitYear;
    }

    public void setVisitYear(Integer visitYear) {
        this.visitYear = visitYear;
    }

    public Integer getVisitMonth() {
        return visitMonth;
    }

    public void setVisitMonth(Integer visitMonth) {
        this.visitMonth = visitMonth;
    }
}