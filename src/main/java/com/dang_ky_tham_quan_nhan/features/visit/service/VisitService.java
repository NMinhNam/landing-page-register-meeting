package com.dang_ky_tham_quan_nhan.features.visit.service;

import com.dang_ky_tham_quan_nhan.common.enums.VisitStatus;
import com.dang_ky_tham_quan_nhan.features.auth.entity.AdminUser;
import com.dang_ky_tham_quan_nhan.features.auth.mapper.AdminUserMapper;
import com.dang_ky_tham_quan_nhan.features.unit.entity.Unit;
import com.dang_ky_tham_quan_nhan.features.unit.mapper.UnitMapper;
import com.dang_ky_tham_quan_nhan.features.unit.service.UnitService;
import com.dang_ky_tham_quan_nhan.features.visit.dto.AdminVisitResponse;
import com.dang_ky_tham_quan_nhan.features.visit.dto.RegistrationRequest;
import com.dang_ky_tham_quan_nhan.features.visit.dto.UpdateStatusRequest;
import com.dang_ky_tham_quan_nhan.features.visit.entity.Relative;
import com.dang_ky_tham_quan_nhan.features.visit.entity.VisitRegistration;
import com.dang_ky_tham_quan_nhan.features.visit.mapper.RelativeMapper;
import com.dang_ky_tham_quan_nhan.features.visit.mapper.VisitRegistrationMapper;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
                reg.setUnitId(unit.getId());
            } else {
            }
        }

        reg.setRepresentativePhone(request.getRepresentativePhone());
        reg.setProvince(request.getProvince());

        // Calculate week and month
        LocalDateTime now = LocalDateTime.now();
        WeekMonth weekMonth = calculateWeekOfMonth(now);
        reg.setVisitWeek(weekMonth.week);
        // Store month for reference (format: MM, 01-12)

        // Calculate and store the display values
        String weekMonthDisplay = calculateWeekMonthDisplay(weekMonth.week, now);
        reg.setVisitWeekMonthDisplay(weekMonthDisplay);

        // Extract month from the calculated display (format: "week/month")
        String[] parts = weekMonthDisplay.split("/");
        if (parts.length == 2) {
            try {
                int displayMonth = Integer.parseInt(parts[1]);
                reg.setVisitMonth(displayMonth);
            } catch (NumberFormatException e) {
                // Fallback to actual month if parsing fails
                reg.setVisitMonth(now.getMonthValue());
            }
        } else {
            // Fallback to actual month if format is unexpected
            reg.setVisitMonth(now.getMonthValue());
        }

        reg.setVisitYear(now.getYear());

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

    @Transactional(readOnly = true)
    public List<AdminVisitResponse> searchAdmin(Long unitId, String month,
            Integer week, String year, String province,
            String status, String keyword, Long adminId) {

        System.out.println("[DEBUG] searchAdmin called with adminId=" + adminId);

        if (adminId == null) {
            return new ArrayList<>();
        }

        // Verify admin exists
        AdminUser admin = adminUserMapper.findById(adminId);
        if (admin == null) {
            System.out.println("[DEBUG] Admin not found");
            throw new RuntimeException("Admin không hợp lệ");
        }

        // Nếu admin không có unit_id, không có quyền xem danh sách
        if (admin.getUnitId() == null) {
            System.out.println("[DEBUG] Admin unitId is null");
            return new java.util.ArrayList<>();
        }

        // Use WITH RECURSIVE in mapper to get all child units
        List<AdminVisitResponse> results = visitRegistrationMapper
                .searchAdmin(adminId, month, week, year, province,
                        status, keyword);

        System.out.println("[DEBUG] searchAdmin results size: " + results.size());
        return results;
    }

    public String calculateWeekMonthDisplay(Integer week, java.time.LocalDateTime createdAt) {
        if (createdAt == null)
            return "Tuần " + week;

        int dayOfMonth = createdAt.getDayOfMonth();
        int year = createdAt.getYear();

        // Get first day of month (1=Monday, 7=Sunday)
        java.time.LocalDate firstDay = java.time.LocalDate.of(year, createdAt.getMonth(), 1);
        int firstDayOfWeek = firstDay.getDayOfWeek().getValue(); // 1=Mon, 7=Sun
        int firstDayOfMonth = firstDayOfWeek; // 1=Monday, 7=Sunday

        int month = createdAt.getMonthValue(); // 1-12
        int calculatedYear = year;

        // Logic: If date is before first Monday, it belongs to previous month
        if (firstDayOfMonth == 7) {
            // First day is Sunday
            if (dayOfMonth == 1) {
                // Day 1 (Sunday) belongs to previous month
                month = month - 1;
                if (month == 0) {
                    month = 12;
                    calculatedYear = year - 1; // Adjust year when moving to previous month
                }
            }
        } else {
            // First day is Monday-Saturday
            int daysBeforeFirstMonday = (8 - firstDayOfMonth) % 7;
            if (dayOfMonth <= daysBeforeFirstMonday) {
                // Belongs to previous month
                month = month - 1;
                if (month == 0) {
                    month = 12;
                    calculatedYear = year - 1; // Adjust year when moving to previous month
                }
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
        result.put("visitWeekMonthDisplay", reg.getVisitWeekMonthDisplay());
        result.put("visitYear", reg.getVisitYear());
        result.put("visitMonth", reg.getVisitMonth());
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

            // If registration has no unitId (unlikely with new logic, but possible for old
            // data), block it
            if (reg.getUnitId() == null) {
                // Fallback: Check name match if ID is missing (legacy support)
                Unit adminUnit = unitMapper.findById(admin.getUnitId());
                String adminUnitName = (adminUnit != null) ? adminUnit.getName() : "";
                if (!adminUnitName.equalsIgnoreCase(reg.getManualUnitName())) {
                    throw new RuntimeException("Đơn này không thuộc phạm vi quản lý của bạn (Không xác định đơn vị).");
                }
            } else {
                if (!allowedUnitIds.contains(reg.getUnitId())) {
                    throw new RuntimeException(
                            "Bạn không có quyền xử lý đơn của đơn vị này (nằm ngoài phạm vi quản lý).");
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

    @Transactional(readOnly = true)
    public Map<String, Object> getStats(String month, Integer week, String year, Long adminId) {
        if (adminId == null) {
            return new HashMap<>();
        }

        AdminUser admin = adminUserMapper.findById(adminId);
        if (admin == null) {
            throw new RuntimeException("Admin không hợp lệ");
        }

        // Nếu admin không có unit_id, không có quyền xem thống kê
        if (admin.getUnitId() == null) {
            return Map.of("byProvince", new java.util.ArrayList<>(), "byStatus", new java.util.ArrayList<>());
        }

        // Get all records first, then apply year/week/month filtering in memory
        List<AdminVisitResponse> allData = visitRegistrationMapper
                .searchAdmin(adminId, null, null, null, null, null, null);

        System.out.println("[DEBUG] getStats - initial load size: " + allData.size());

        // Apply year/week/month filtering based on the stored display values
        if ((year != null && !year.isEmpty()) || (month != null && !month.isEmpty()) || week != null) {
            allData = allData.stream()
                    .filter(result -> {
                        Integer resultWeek = result.getVisitWeek();
                        Integer resultYear = result.getVisitYear();
                        Integer resultMonth = result.getVisitMonth();

                        if (resultWeek == null || resultYear == null || resultMonth == null) {
                            return false;
                        }

                        // Check if the stored values match the filter criteria
                        boolean yearMatch = (year == null) || (resultYear.toString().equals(year));
                        boolean weekMatch = (week == null) || (resultWeek.equals(week));
                        boolean monthMatch = (month == null) || (resultMonth.toString().equals(month));

                        return yearMatch && weekMatch && monthMatch;
                    })
                    .collect(java.util.stream.Collectors.toList());
        }

        // Calculate stats from filtered data
        Map<String, Object> stats = new HashMap<>();

        // Calculate byProvince stats
        Map<String, Long> provinceCount = allData.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        row -> row.getProvince() != null ? row.getProvince() : "N/A",
                        java.util.stream.Collectors.counting()));

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
                        row -> row.getStatus() != null ? row.getStatus() : "N/A",
                        java.util.stream.Collectors.counting()));

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

    public byte[] exportRegistrations(Long unitId, String month, Integer week, String year, String province,
            String status, Long adminId) {
        List<com.dang_ky_tham_quan_nhan.features.visit.dto.AdminVisitResponse> data = searchAdmin(unitId, month, week,
                year, province, status, null, adminId);

        try (org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Danh sách đăng ký");

            // Create title row
            org.apache.poi.ss.usermodel.Row titleRow = sheet.createRow(0);
            String title = "DANH SÁCH ĐĂNG KÝ THĂM QUÂN NHÂN TUẦN " + (week != null ? week : "...") + " THÁNG "
                    + (month != null ? month : "...") + " NĂM " + (year != null ? year : "...") + " CỦA TIỂU ĐOÀN 4";
            org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(title);

            // Merge cells for title
            CellRangeAddress region = new CellRangeAddress(0, 0, 0, 10);
            sheet.addMergedRegion(region);

            // Style for title
            org.apache.poi.ss.usermodel.CellStyle titleStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
            titleStyle.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
            titleCell.setCellStyle(titleStyle);

            // Create header row (now at row 1)
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(1);
            String[] headers = { "STT", "Tên quân nhân", "Chức vụ", "Đơn vị", "Tỉnh/ Thành phố", "Thân nhân", "Quan hệ",
                    "Mã số định danh/CCCD", "SDT người đại diện", "Trạng thái", "Ghi chú" };

            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);

                // Apply styling to header cells
                org.apache.poi.ss.usermodel.CellStyle headerStyle = workbook.createCellStyle();
                org.apache.poi.ss.usermodel.Font font = workbook.createFont();
                font.setBold(true);
                headerStyle.setFont(font);
                cell.setCellStyle(headerStyle);
            }

            // Populate data rows (starting from row 2)
            int rowNum = 2;
            for (com.dang_ky_tham_quan_nhan.features.visit.dto.AdminVisitResponse row : data) {
                // Get the concatenated relatives and relationships from the query result
                String relativeNames = getStringValue(row.getRelativeName());
                String relationships = getStringValue(row.getRelationships());

                // Split the concatenated strings by comma to get individual values
                String[] namesArray = relativeNames.isEmpty() ? new String[0] : relativeNames.split(",");
                String[] relationshipsArray = relationships.isEmpty() ? new String[0] : relationships.split(",");

                // Determine the number of rows needed based on the maximum of names or
                // relationships
                int maxRows = Math.max(namesArray.length, relationshipsArray.length);

                if (maxRows == 0) {
                    // If there are no relatives, create a single row with empty values
                    org.apache.poi.ss.usermodel.Row dataRow = sheet.createRow(rowNum++);

                    int cellNum = 0;

                    // STT
                    dataRow.createCell(cellNum++).setCellValue(rowNum - 2); // -2 because we start from row 2

                    // Tên quân nhân
                    dataRow.createCell(cellNum++).setCellValue(getStringValue(row.getSoldierName()));

                    // Chức vụ (chưa có trong DB, tạm để trống hoặc lấy từ thông tin quân nhân nếu
                    // có)
                    dataRow.createCell(cellNum++).setCellValue(""); // Chưa có trong DB, cần thêm nếu có

                    // Đơn vị
                    dataRow.createCell(cellNum++).setCellValue(getStringValue(row.getUnitName()));

                    // Tỉnh/Thành phố
                    dataRow.createCell(cellNum++).setCellValue(getStringValue(row.getProvince()));

                    // Thân nhân
                    dataRow.createCell(cellNum++).setCellValue("");

                    // Quan hệ
                    dataRow.createCell(cellNum++).setCellValue("");

                    // Mã số định danh/CCCD
                    dataRow.createCell(cellNum++).setCellValue("");

                    // SDT người đại diện
                    dataRow.createCell(cellNum++).setCellValue(getStringValue(row.getRelativePhone()));

                    // Trạng thái
                    dataRow.createCell(cellNum++).setCellValue(getStringValue(row.getStatus()));

                    // Ghi chú
                    dataRow.createCell(cellNum++).setCellValue(getStringValue(row.getNote()));
                } else {
                    // For each relative/relationship pair, create a separate row
                    for (int i = 0; i < maxRows; i++) {
                        org.apache.poi.ss.usermodel.Row dataRow = sheet.createRow(rowNum++);

                        int cellNum = 0;

                        // STT
                        dataRow.createCell(cellNum++).setCellValue(rowNum - 2); // -2 because we start from row 2

                        // Tên quân nhân
                        dataRow.createCell(cellNum++).setCellValue(getStringValue(row.getSoldierName()));

                        // Chức vụ (chưa có trong DB, tạm để trống hoặc lấy từ thông tin quân nhân nếu
                        // có)
                        dataRow.createCell(cellNum++).setCellValue(""); // Chưa có trong DB, cần thêm nếu có

                        // Đơn vị
                        dataRow.createCell(cellNum++).setCellValue(getStringValue(row.getUnitName()));

                        // Tỉnh/Thành phố
                        dataRow.createCell(cellNum++).setCellValue(getStringValue(row.getProvince()));

                        // Thân nhân
                        dataRow.createCell(cellNum++).setCellValue(i < namesArray.length ? namesArray[i].trim() : "");

                        // Quan hệ
                        dataRow.createCell(cellNum++)
                                .setCellValue(i < relationshipsArray.length ? relationshipsArray[i].trim() : "");

                        // Mã số định danh/CCCD
                        dataRow.createCell(cellNum++)
                                .setCellValue(i < namesArray.length ? getStringValue(row.getRelativeIds()) : "");

                        // SDT người đại diện
                        dataRow.createCell(cellNum++).setCellValue(getStringValue(row.getRelativePhone()));

                        // Trạng thái
                        dataRow.createCell(cellNum++).setCellValue(getStringValue(row.getStatus()));

                        // Ghi chú
                        dataRow.createCell(cellNum++).setCellValue(getStringValue(row.getNote()));
                    }
                }
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to byte array
            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error exporting to Excel", e);
        }
    }

    private String getStringValue(Object obj) {
        return obj != null ? obj.toString() : "";
    }

    private int getIntegerValue(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        } else if (obj != null) {
            try {
                return Integer.parseInt(obj.toString());
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
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
