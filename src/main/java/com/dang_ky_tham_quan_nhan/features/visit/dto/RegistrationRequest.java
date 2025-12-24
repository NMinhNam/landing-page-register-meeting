package com.dang_ky_tham_quan_nhan.features.visit.dto;

import java.util.List;

public class RegistrationRequest {
    private String manualSoldierName;
    private String manualUnitName;
    private String representativePhone;
    private String province;
    private Integer visitWeek;
    private List<RelativeInfo> relatives;

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

    public List<RelativeInfo> getRelatives() {
        return relatives;
    }

    public void setRelatives(List<RelativeInfo> relatives) {
        this.relatives = relatives;
    }

    public static class RelativeInfo {
        private String name;
        private String relationship;
        private String idNumber;

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
}
