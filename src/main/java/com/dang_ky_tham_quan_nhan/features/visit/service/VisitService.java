package com.dang_ky_tham_quan_nhan.features.visit.service;

import com.dang_ky_tham_quan_nhan.common.enums.VisitStatus;
import com.dang_ky_tham_quan_nhan.features.unit.entity.Unit;
import com.dang_ky_tham_quan_nhan.features.unit.mapper.UnitMapper;
import com.dang_ky_tham_quan_nhan.features.visit.dto.RegistrationRequest;
import com.dang_ky_tham_quan_nhan.features.visit.dto.UpdateStatusRequest;
import com.dang_ky_tham_quan_nhan.features.visit.entity.Relative;
import com.dang_ky_tham_quan_nhan.features.visit.entity.VisitRegistration;
import com.dang_ky_tham_quan_nhan.features.visit.mapper.RelativeMapper;
import com.dang_ky_tham_quan_nhan.features.visit.mapper.VisitRegistrationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VisitService {

    private final RelativeMapper relativeMapper;
    private final VisitRegistrationMapper visitRegistrationMapper;
    private final UnitMapper unitMapper;

    public VisitService(RelativeMapper relativeMapper, VisitRegistrationMapper visitRegistrationMapper, UnitMapper unitMapper) {
        this.relativeMapper = relativeMapper;
        this.visitRegistrationMapper = visitRegistrationMapper;
        this.unitMapper = unitMapper;
    }

    @Transactional
    public VisitRegistration registerVisit(RegistrationRequest request) {
        // 1. Create Registration
        VisitRegistration reg = new VisitRegistration();
        
        // Manual Soldier & Unit Logic - users input directly
        reg.setSoldierId(null);
        reg.setManualSoldierName(request.getManualSoldierName());
        reg.setManualUnitName(request.getManualUnitName());

        reg.setRepresentativePhone(request.getRepresentativePhone());
        reg.setProvince(request.getProvince());
        
        // Calculate current week
        int currentWeek = (LocalDateTime.now().getDayOfMonth() - 1) / 7 + 1;
        reg.setVisitWeek(currentWeek);
        
        reg.setStatus(VisitStatus.PENDING.name());
        reg.setCreatedAt(LocalDateTime.now());
        
        visitRegistrationMapper.insert(reg);

        // 2. Add Relatives
        if (request.getRelatives() != null) {
            for (RegistrationRequest.RelativeInfo info : request.getRelatives()) {
                Relative r = new Relative();
                r.setVisitRegistrationId(reg.getId());
                r.setName(info.getName());
                r.setRelationship(info.getRelationship());
                r.setIdNumber(info.getIdNumber());
                relativeMapper.insert(r);
            }
        }
        return reg;
    }

    public List<Map<String, Object>> searchRegistration(String phone, String code) {
        return visitRegistrationMapper.findByPhone(phone);
    }
    
    public List<Map<String, Object>> searchAdmin(Long unitId, Integer week, String province, String status, String keyword) {
        return visitRegistrationMapper.searchAdmin(unitId, week, province, status, keyword);
    }

    public void updateStatus(Long id, UpdateStatusRequest request) {
        VisitRegistration reg = visitRegistrationMapper.findById(id);
        if (reg != null) {
            reg.setStatus(request.getStatus().name());
            reg.setNote(request.getNote());
            reg.setApprovedAt(LocalDateTime.now());
            visitRegistrationMapper.update(reg);
        } else {
            throw new RuntimeException("Not Found");
        }
    }

    public void deleteRegistration(Long id) {
        visitRegistrationMapper.deleteById(id);
    }

    public void cancelRegistration(Long id) {
        VisitRegistration reg = visitRegistrationMapper.findById(id);
        if (reg != null) {
            if (!VisitStatus.PENDING.name().equals(reg.getStatus())) {
                throw new RuntimeException("Chỉ có thể hủy đơn đang chờ duyệt");
            }
            reg.setStatus("CANCELLED"); // Assuming CANCELLED enum exists or string usage
            visitRegistrationMapper.update(reg);
        } else {
            throw new RuntimeException("Not Found");
        }
    }

    public Map<String, Object> getStats(Integer week) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("byProvince", visitRegistrationMapper.countByProvince(week));
        stats.put("byStatus", visitRegistrationMapper.countByStatus(week));
        return stats;
    }
}
