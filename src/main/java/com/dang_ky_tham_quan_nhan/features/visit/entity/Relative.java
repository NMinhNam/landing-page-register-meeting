package com.dang_ky_tham_quan_nhan.features.visit.entity;

import java.io.Serializable;

public class Relative implements Serializable {
    private Long id;
    private Long visitRegistrationId;
    private String name;
    private String relationship;
    private String idNumber;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVisitRegistrationId() {
        return visitRegistrationId;
    }

    public void setVisitRegistrationId(Long visitRegistrationId) {
        this.visitRegistrationId = visitRegistrationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }
}