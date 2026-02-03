package com.dang_ky_tham_quan_nhan.features.visit.dto;

import java.time.LocalDateTime;

public class AdminVisitResponse {
    private Long id;
    private Long soldierId;
    private Long unitId;
    private String manualSoldierName;
    private String manualUnitName;
    private String representativePhone;
    private String province;
    private Integer visitWeek;
    private String visitWeekMonthDisplay;
    private Integer visitYear;
    private Integer visitMonth;
    private String status;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;

    // Computed / Joined fields
    private String soldierName;
    private String unitName;
    private String relativeName;
    private String relativeIds;
    private String relationships;
    private String relativePhone;

    // Getters and Setters
    public Long getId() {
        return id;
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

    public String getManualSoldierName() {
        return manualSoldierName;
    }

    public void setManualSoldierName(String manualSoldierName) {
        this.manualSoldierName = manualSoldierName;
    }

    public String getManualUnitName() {
        return manualUnitName;
    }

    public void setManualUnitName(String manualUnitName) {
        this.manualUnitName = manualUnitName;
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

    public String getSoldierName() {
        return soldierName;
    }

    public void setSoldierName(String soldierName) {
        this.soldierName = soldierName;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public String getRelativeName() {
        return relativeName;
    }

    public void setRelativeName(String relativeName) {
        this.relativeName = relativeName;
    }

    public String getRelativeIds() {
        return relativeIds;
    }

    public void setRelativeIds(String relativeIds) {
        this.relativeIds = relativeIds;
    }

    public String getRelationships() {
        return relationships;
    }

    public void setRelationships(String relationships) {
        this.relationships = relationships;
    }

    public String getRelativePhone() {
        return relativePhone;
    }

    public void setRelativePhone(String relativePhone) {
        this.relativePhone = relativePhone;
    }
}
