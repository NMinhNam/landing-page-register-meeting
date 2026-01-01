package com.dang_ky_tham_quan_nhan.features.visit.service;

import com.dang_ky_tham_quan_nhan.common.enums.VisitStatus;
import com.dang_ky_tham_quan_nhan.features.auth.entity.AdminUser;
import com.dang_ky_tham_quan_nhan.features.auth.mapper.AdminUserMapper;
import com.dang_ky_tham_quan_nhan.features.unit.entity.Unit;
import com.dang_ky_tham_quan_nhan.features.unit.mapper.UnitMapper;
import com.dang_ky_tham_quan_nhan.features.unit.service.UnitService;
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
    private final AdminUserMapper adminUserMapper;

    private final UnitService unitService;

    public VisitService(RelativeMapper relativeMapper,
                        VisitRegistrationMapper visitRegistrationMapper,
                        UnitMapper unitMapper,
                        AdminUserMapper adminUserMapper,
                        UnitService unitService) {
        this.relativeMapper = relativeMapper;
        this.visitRegistrationMapper = visitRegistrationMapper;
        this.unitMapper = unitMapper;
        this.adminUserMapper = adminUserMapper;
        this.unitService = unitService;
    }

    @Transactional
    public VisitRegistration registerVisit(RegistrationRequest request) {
        // 1. Create Registration
        VisitRegistration reg = new VisitRegistration();
        
        // Manual Soldier & Unit Logic - users input directly
        reg.setSoldierId(null);
        reg.setManualSoldierName(request.getManualSoldierName());
        reg.setManualUnitName(request.getManualUnitName());
        
        // Lookup Unit ID strictly by Name (Since frontend sends Name)
        if (request.getManualUnitName() != null) {
            Unit unit = unitMapper.findByName(request.getManualUnitName());
            if (unit != null) {
                reg.setUnitId(unit.getId());
            }
        }

        reg.setRepresentativePhone(request.getRepresentativePhone());
        reg.setProvince(request.getProvince());
        int currentWeek = (LocalDateTime.now().getDayOfMonth() - 1) / 7 + 1;
        reg.setVisitWeek(currentWeek);
        reg.setStatus(VisitStatus.PENDING.name());
        reg.setCreatedAt(LocalDateTime.now());
        visitRegistrationMapper.insert(reg);

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

        public List<Map<String, Object>> searchAdmin(Long unitId, Integer week, String province, String status, String keyword, Long adminId) {
            System.out.println("[DEBUG] searchAdmin called. Params: unitId=" + unitId + ", adminId=" + adminId);
            
            // 1. Determine the root Unit ID to filter by
            Long rootUnitId = unitId;
    
            // Security Check: Enforce Unit Filter if Admin belongs to a specific unit
            if (adminId != null) {
                AdminUser admin = adminUserMapper.findById(adminId);
                if (admin != null) {
                    System.out.println("[DEBUG] Admin Found: " + admin.getUsername() + ", UnitID: " + admin.getUnitId());
                    if (admin.getUnitId() != null) {
                        rootUnitId = admin.getUnitId(); // FORCE FILTER
                    }
                } else {
                     System.out.println("[DEBUG] Admin NOT found for ID: " + adminId);
                }
            }
            
            // 2. Get all child Unit IDs (Recursive)
            List<Long> targetUnitIds = null;
            if (rootUnitId != null) {
                targetUnitIds = unitService.getAllChildUnitIds(rootUnitId);
                System.out.println("[DEBUG] Target Unit IDs (Allowed): " + targetUnitIds);
            }
    
            List<Map<String, Object>> results = visitRegistrationMapper.searchAdmin(targetUnitIds, week, province, status, keyword);
            
            // FINAL SECURITY CHECK (Java Level)
            if (targetUnitIds != null && !targetUnitIds.isEmpty()) {
                List<Long> allowedIds = targetUnitIds;
                results.removeIf(row -> {
                    // Try getting ID with both naming conventions (snake_case vs camelCase)
                    Object objId = row.get("unit_id");
                    if (objId == null) objId = row.get("unitId"); // Case for map-underscore-to-camel-case=true
                    
                    Long rowUnitId = (objId instanceof Number) ? ((Number) objId).longValue() : null;
                    
                    if (rowUnitId == null) {
                        // Safe approach: If Admin is strict Unit Admin, HIDE records with unknown unit.
                        return true; 
                    }
                    
                    boolean isAllowed = allowedIds.contains(rowUnitId);
                    if (!isAllowed) {
                         System.out.println("[SECURITY BLOCK] Removed Record ID: " + row.get("id") + " with UnitID: " + rowUnitId);
                    }
                    return !isAllowed;
                });
            }
            
            return results;
        }
    public Map<String, Object> getRegistrationDetail(Long id) {
        VisitRegistration reg = visitRegistrationMapper.findById(id);
        if (reg == null) {
            throw new RuntimeException("Không tìm thấy đơn đăng ký");
        }

        List<Relative> relatives = relativeMapper.findByRegistrationId(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", reg.getId());
        result.put("manualSoldierName", reg.getManualSoldierName());
        result.put("manualUnitName", reg.getManualUnitName());
        result.put("representativePhone", reg.getRepresentativePhone());
        result.put("province", reg.getProvince());
        result.put("visitWeek", reg.getVisitWeek());
        result.put("status", reg.getStatus());
        result.put("note", reg.getNote());
        result.put("createdAt", reg.getCreatedAt());
        result.put("approvedAt", reg.getApprovedAt());
        result.put("relatives", relatives);

        return result;
    }

    public void updateStatus(Long id, UpdateStatusRequest request) {
        VisitRegistration reg = visitRegistrationMapper.findById(id);
        if (reg == null) {
            throw new RuntimeException("Không tìm thấy đơn đăng ký");
        }

        // Check Permissions
        AdminUser admin = adminUserMapper.findById(request.getAdminId());
        if (admin == null) {
            throw new RuntimeException("Admin không hợp lệ hoặc phiên làm việc hết hạn");
        }

        // Nếu admin có unitId (Admin cấp đơn vị), kiểm tra xem đơn đăng ký có thuộc đơn vị đó không
        if (admin.getUnitId() != null) {
            // Lấy tên đơn vị của admin để so sánh
            Unit adminUnit = unitMapper.findById(admin.getUnitId());
            String adminUnitName = (adminUnit != null) ? adminUnit.getName() : "";

            boolean isSameUnit = false;
            // So sánh manualUnitName hoặc thông qua soldier_id (nếu có)
            if (adminUnitName.equalsIgnoreCase(reg.getManualUnitName())) {
                isSameUnit = true;
            }

            // Bạn có thể thêm logic check soldier.unitId ở đây nếu cần

            if (!isSameUnit) {
                throw new RuntimeException("Bạn không có quyền xử lý đơn của đơn vị khác");
            }
        }

        reg.setStatus(request.getStatus().name());
        reg.setNote(request.getNote());
        reg.setApprovedAt(LocalDateTime.now());
        visitRegistrationMapper.update(reg);
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

    public byte[] exportRegistrations(Long unitId, Integer week, String province, String status, Long adminId) {
        List<Map<String, Object>> data = searchAdmin(unitId, week, province, status, null, adminId);
        
        StringBuilder csv = new StringBuilder();
        // Add BOM for Excel UTF-8 compatibility
        csv.append("\uFEFF");
        // Header
        csv.append("STT,Tên đơn vị,Tỉnh/Thành phố,Danh sách thân nhân,Mã số định danh/CCCD,SĐT người đại diện,Trạng thái,Ghi chú\n");
        
        int stt = 1;
        for (Map<String, Object> row : data) {
            csv.append(stt++).append(",");
            csv.append(escapeCsv((String) row.get("unit_name"))).append(",");
            csv.append(escapeCsv((String) row.get("province"))).append(",");
            csv.append(escapeCsv((String) row.get("relative_name"))).append(",");
            csv.append(escapeCsv((String) row.get("relative_ids"))).append(",");
            csv.append(escapeCsv((String) row.get("relative_phone"))).append(",");
            csv.append(escapeCsv((String) row.get("status"))).append(",");
            csv.append(escapeCsv((String) row.get("note"))).append("\n");
        }
        
        return csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private String escapeCsv(String val) {
        if (val == null) return "";
        String escaped = val.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }
}
