package com.dang_ky_tham_quan_nhan.features.visit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Request object for visit registration")
public class RegistrationRequest {
    
    @Schema(description = "Name of the soldier (manually entered if not found)", example = "Tran Van B")
    private String manualSoldierName;
    
    @Schema(description = "Name of the unit (manually entered if not found)", example = "Company 3")
    private String manualUnitName;
    
    @Schema(description = "Phone number of the representative relative", example = "0987654321")
    private String representativePhone;
    
    @Schema(description = "Province of the representative", example = "Hanoi")
    private String province;
    
    @Schema(description = "Week number requested for visit (1-4)", example = "2")
    private Integer visitWeek;
    
    @Schema(description = "List of relatives visiting")
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

    @Schema(description = "Information about a relative")
    public static class RelativeInfo {
        
        @Schema(description = "Name of the relative", example = "Nguyen Thi C")
        private String name;
        
        @Schema(description = "Relationship with the soldier", example = "Mother")
        private String relationship;
        
        @Schema(description = "ID number of the relative", example = "123456789")
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