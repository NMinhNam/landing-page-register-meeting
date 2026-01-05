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
        
        String manualUnitName = request.getManualUnitName() != null ? request.getManualUnitName().trim() : null;
        reg.setManualUnitName(manualUnitName);
        
        // Lookup Unit ID strictly by Name (Since frontend sends Name)
        if (manualUnitName != null) {
            Unit unit = unitMapper.findByName(manualUnitName);
            if (unit != null) {
                System.out.println("[REGISTER] Found Unit ID: " + unit.getId() + " for Name: " + manualUnitName);
                reg.setUnitId(unit.getId());
            } else {
                System.out.println("[REGISTER] WARNING: Unit NOT found for Name: " + manualUnitName);
            }
        }

        reg.setRepresentativePhone(request.getRepresentativePhone());
        reg.setProvince(request.getProvince());
        
        // Calculate week and month
        LocalDateTime now = LocalDateTime.now();
        WeekMonth weekMonth = calculateWeekOfMonth(now);
        reg.setVisitWeek(weekMonth.week);
        // Store month for reference (format: MM, 01-12)
        
        reg.setStatus(VisitStatus.PENDING.name());
        reg.setCreatedAt(now);
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

    public List<Relative> getRelativesByRegistrationId(Long registrationId) {
        return relativeMapper.findByRegistrationId(registrationId);
    }

        public List<Map<String, Object>> searchAdmin(Long unitId, String month, Integer week, String province, String status, String keyword, Long adminId) {

        if (adminId == null) {
            return new java.util.ArrayList<>();
        }
        
        // Verify admin exists
        AdminUser admin = adminUserMapper.findById(adminId);
        if (admin == null) {
            throw new RuntimeException("Admin không hợp lệ");
        }
        

        // Nếu admin không có unit_id, không có quyền xem danh sách
        if (admin.getUnitId() == null) {
            System.out.println("[DEBUG] Admin không có unit_id, trả về danh sách rỗng");
            return new java.util.ArrayList<>();
        }
        
        // Use WITH RECURSIVE in mapper to get all child units
        List<Map<String, Object>> results = visitRegistrationMapper.searchAdmin(adminId, null, null, province, status, keyword);

        // Apply week/month filtering based on the frontend display logic
        if ((month != null && !month.isEmpty()) || week != null) {
            results = results.stream()
                .filter(result -> {
                    Integer resultWeek = result.get("visit_week") != null ?
                        Integer.valueOf(result.get("visit_week").toString()) : null;
                    String createdAtStr = result.get("created_at") != null ?
                        result.get("created_at").toString() : null;

                    if (resultWeek == null || createdAtStr == null) {
                        return false;
                    }

                    // Parse the created_at date
                    java.time.LocalDateTime createdAt = java.time.LocalDateTime.parse(
                        createdAtStr.replace(" ", "T"));

                    // Calculate the display week/month using the same logic as the frontend
                    String calculatedDisplay = calculateWeekMonthDisplay(resultWeek, createdAt);

                    // Check if the calculated display matches the filter criteria
                    boolean weekMatch = (week == null) || (resultWeek.equals(week));
                    boolean monthMatch = true;

                    if (month != null && !month.isEmpty()) {
                        // Extract month from calculated display (format: "week/month")
                        String[] parts = calculatedDisplay.split("/");
                        if (parts.length == 2) {
                            String displayMonth = parts[1];
                            monthMatch = month.equals(displayMonth);
                        } else {
                            monthMatch = false;
                        }
                    }

                    return weekMatch && monthMatch;
                })
                .collect(java.util.stream.Collectors.toList());
        }

        return results;
    }

    private String calculateWeekMonthDisplay(Integer week, java.time.LocalDateTime createdAt) {
        if (createdAt == null) return "Tuần " + week;

        int dayOfMonth = createdAt.getDayOfMonth();

        // Get first day of month (1=Monday, 7=Sunday)
        java.time.LocalDate firstDay = java.time.LocalDate.of(createdAt.getYear(), createdAt.getMonth(), 1);
        int firstDayOfWeek = firstDay.getDayOfWeek().getValue(); // 1=Mon, 7=Sun
        int firstDayOfMonth = firstDayOfWeek; // 1=Monday, 7=Sunday

        int month = createdAt.getMonthValue(); // 1-12

        // Logic: If date is before first Monday, it belongs to previous month
        if (firstDayOfMonth == 7) {
            // First day is Sunday
            if (dayOfMonth == 1) {
                // Day 1 (Sunday) belongs to previous month
                month = month - 1;
                if (month == 0) month = 12;
            }
        } else {
            // First day is Monday-Saturday
            int daysBeforeFirstMonday = (8 - firstDayOfMonth) % 7;
            if (dayOfMonth <= daysBeforeFirstMonday) {
                // Belongs to previous month
                month = month - 1;
                if (month == 0) month = 12;
            }
        }

        String monthStr = String.format("%02d", month);
        return week + "/" + monthStr;
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

        // 1. Role Check: Only ADMIN can update status
        if (!"ADMIN".equals(admin.getRole())) {
            throw new RuntimeException("Bạn chỉ có quyền xem, không được phép duyệt/từ chối.");
        }

        // 2. Unit Hierarchy Check
        if (admin.getUnitId() != null) {
            List<Long> allowedUnitIds = unitService.getAllChildUnitIds(admin.getUnitId());
            
            // If registration has no unitId (unlikely with new logic, but possible for old data), block it
            if (reg.getUnitId() == null) {
                 // Fallback: Check name match if ID is missing (legacy support)
                 Unit adminUnit = unitMapper.findById(admin.getUnitId());
                 String adminUnitName = (adminUnit != null) ? adminUnit.getName() : "";
                 if (!adminUnitName.equalsIgnoreCase(reg.getManualUnitName())) {
                     throw new RuntimeException("Đơn này không thuộc phạm vi quản lý của bạn (Không xác định đơn vị).");
                 }
            } else {
                if (!allowedUnitIds.contains(reg.getUnitId())) {
                    throw new RuntimeException("Bạn không có quyền xử lý đơn của đơn vị này (nằm ngoài phạm vi quản lý).");
                }
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

    public Map<String, Object> getStats(String month, Integer week, Long adminId) {
        if (adminId == null) {
            return new HashMap<>();
        }

        AdminUser admin = adminUserMapper.findById(adminId);
        if (admin == null) {
            throw new RuntimeException("Admin không hợp lệ");
        }

        // Nếu admin không có unit_id, không có quyền xem thống kê
        if (admin.getUnitId() == null) {
            System.out.println("[DEBUG] Admin " + admin.getUsername() + " không có unit_id, trả về stats rỗng");
            return Map.of("byProvince", new java.util.ArrayList<>(), "byStatus", new java.util.ArrayList<>());
        }

        // Get all records first, then apply week/month filtering in memory
        List<Map<String, Object>> allData = visitRegistrationMapper.searchAdmin(adminId, null, null, null, null, null);

        // Apply week/month filtering based on the frontend display logic
        if ((month != null && !month.isEmpty()) || week != null) {
            allData = allData.stream()
                .filter(result -> {
                    Integer resultWeek = result.get("visit_week") != null ?
                        Integer.valueOf(result.get("visit_week").toString()) : null;
                    String createdAtStr = result.get("created_at") != null ?
                        result.get("created_at").toString() : null;

                    if (resultWeek == null || createdAtStr == null) {
                        return false;
                    }

                    // Parse the created_at date
                    java.time.LocalDateTime createdAt = java.time.LocalDateTime.parse(
                        createdAtStr.replace(" ", "T"));

                    // Calculate the display week/month using the same logic as the frontend
                    String calculatedDisplay = calculateWeekMonthDisplay(resultWeek, createdAt);

                    // Check if the calculated display matches the filter criteria
                    boolean weekMatch = (week == null) || (resultWeek.equals(week));
                    boolean monthMatch = true;

                    if (month != null && !month.isEmpty()) {
                        // Extract month from calculated display (format: "week/month")
                        String[] parts = calculatedDisplay.split("/");
                        if (parts.length == 2) {
                            String displayMonth = parts[1];
                            monthMatch = month.equals(displayMonth);
                        } else {
                            monthMatch = false;
                        }
                    }

                    return weekMatch && monthMatch;
                })
                .collect(java.util.stream.Collectors.toList());
        }

        // Calculate stats from filtered data
        Map<String, Object> stats = new HashMap<>();

        // Calculate byProvince stats
        Map<String, Long> provinceCount = allData.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                row -> row.get("province") != null ? row.get("province").toString() : "N/A",
                java.util.stream.Collectors.counting()
            ));

        List<Map<String, Object>> byProvince = provinceCount.entrySet().stream()
            .map(entry -> {
                Map<String, Object> map = new HashMap<>();
                map.put("province", entry.getKey());
                map.put("count", entry.getValue());
                return map;
            })
            .collect(java.util.stream.Collectors.toList());

        // Calculate byStatus stats
        Map<String, Long> statusCount = allData.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                row -> row.get("status") != null ? row.get("status").toString() : "N/A",
                java.util.stream.Collectors.counting()
            ));

        List<Map<String, Object>> byStatus = statusCount.entrySet().stream()
            .map(entry -> {
                Map<String, Object> map = new HashMap<>();
                map.put("status", entry.getKey());
                map.put("count", entry.getValue());
                return map;
            })
            .collect(java.util.stream.Collectors.toList());

        stats.put("byProvince", byProvince);
        stats.put("byStatus", byStatus);

        return stats;
    }

    public byte[] exportRegistrations(Long unitId, String month, Integer week, String province, String status, Long adminId) {
        List<Map<String, Object>> data = searchAdmin(unitId, month, week, province, status, null, adminId);
        
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

    // Helper class to return both week and month
    private static class WeekMonth {
        int week;
        int month; // 1-12
        
        WeekMonth(int week, int month) {
            this.week = week;
            this.month = month;
        }
    }

    private WeekMonth calculateWeekOfMonth(LocalDateTime date) {
        int dayOfMonth = date.getDayOfMonth();
        int firstDayOfMonth = date.withDayOfMonth(1).getDayOfWeek().getValue(); // 1=Mon, 7=Sun
        
        if (firstDayOfMonth == 7) {
            // First day is Sunday
            if (dayOfMonth == 1) {
                // Day 1 (Sunday) belongs to previous month's week
                LocalDateTime prevMonth = date.minusMonths(1);
                int daysInPrevMonth = java.time.YearMonth.from(prevMonth).lengthOfMonth();
                int firstDayOfPrevMonth = prevMonth.withDayOfMonth(1).getDayOfWeek().getValue();
                int weekOfPrevMonth;
                
                if (firstDayOfPrevMonth == 7) {
                    weekOfPrevMonth = (daysInPrevMonth - 1) / 7 + 1;
                } else {
                    int daysBeforeFirstMondayPrev = (8 - firstDayOfPrevMonth) % 7;
                    weekOfPrevMonth = (daysInPrevMonth - daysBeforeFirstMondayPrev) / 7 + 1;
                }
                return new WeekMonth(weekOfPrevMonth, prevMonth.getMonthValue());
            } else {
                return new WeekMonth((dayOfMonth - 1) / 7 + 1, date.getMonthValue());
            }
        } else {
            // First day is Monday-Saturday
            int daysBeforeFirstMonday = (8 - firstDayOfMonth) % 7;
            
            if (dayOfMonth <= daysBeforeFirstMonday) {
                // Before first Monday, belongs to previous month
                LocalDateTime prevMonth = date.minusMonths(1);
                int daysInPrevMonth = java.time.YearMonth.from(prevMonth).lengthOfMonth();
                int firstDayOfPrevMonth = prevMonth.withDayOfMonth(1).getDayOfWeek().getValue();
                int weekOfPrevMonth;
                
                if (firstDayOfPrevMonth == 7) {
                    weekOfPrevMonth = (daysInPrevMonth - 1) / 7 + 1;
                } else {
                    int daysBeforeFirstMondayPrev = (8 - firstDayOfPrevMonth) % 7;
                    weekOfPrevMonth = (daysInPrevMonth - daysBeforeFirstMondayPrev) / 7 + 1;
                }
                return new WeekMonth(weekOfPrevMonth, prevMonth.getMonthValue());
            } else {
                // From first Monday onwards
                return new WeekMonth((dayOfMonth - daysBeforeFirstMonday) / 7 + 1, date.getMonthValue());
            }
        }
    }
}
