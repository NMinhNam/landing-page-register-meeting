package com.dang_ky_tham_quan_nhan.features.visit.controller;

import com.dang_ky_tham_quan_nhan.features.visit.dto.UpdateStatusRequest;
import com.dang_ky_tham_quan_nhan.features.visit.service.VisitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin Visit", description = "Visit Management APIs for Admins")
public class AdminVisitController {

    private final VisitService visitService;

    public AdminVisitController(VisitService visitService) {
        this.visitService = visitService;
    }

    @GetMapping("/export/registrations")
    @Operation(summary = "Export Registrations", description = "Export registrations to Excel.")
    public ResponseEntity<byte[]> exportRegistrations(
            @RequestParam(required = false) Long unitId,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) Integer week,
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long adminId
    ) {
        try {
            byte[] content = visitService.exportRegistrations(unitId, month, week, year, province, status, adminId);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"ds_dang_ky_tham_gap.xlsx\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(content);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/registrations")
    @Operation(summary = "Search Registrations", description = "Filter and list visit registrations.")
    public Map<String, Object> getRegistrations(
            @RequestParam(required = false) Long unitId,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) Integer week,
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String adminId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        Long parsedAdminId = null;
        if (adminId != null && !adminId.isEmpty() && !"null".equalsIgnoreCase(adminId) && !"undefined".equalsIgnoreCase(adminId)) {
            try {
                parsedAdminId = Long.parseLong(adminId);
            } catch (NumberFormatException e) {

            }
        }

        List<Map<String, Object>> data = visitService.searchAdmin(unitId, month, week, year, province, status, keyword, parsedAdminId);
        return Map.of("data", data, "total", data.size());
    }

    @GetMapping("/registrations/{id}")
    @Operation(summary = "Get Registration Details", description = "Get full details of a visit registration including relatives.")
    public Map<String, Object> getRegistrationDetail(@PathVariable Long id) {
        return visitService.getRegistrationDetail(id);
    }

    @PutMapping("/registrations/{id}/status")
    @Operation(summary = "Update Registration Status", description = "Approve or Reject a visit request.")
    public void updateStatus(@PathVariable Long id, @RequestBody UpdateStatusRequest request) {
        visitService.updateStatus(id, request);
    }

    @DeleteMapping("/registrations/{id}")
    @Operation(summary = "Delete Registration", description = "Remove a registration record.")
    public void deleteRegistration(@PathVariable Long id) {
        visitService.deleteRegistration(id);
    }

    @GetMapping("/stats")
    @Operation(summary = "Get Statistics", description = "Get aggregated statistics for visits.")
    public Map<String, Object> getStats(
            @RequestParam(required = false) String month,
            @RequestParam(required = false) Integer week,
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String adminId
    ) {
        Long parsedAdminId = null;
        if (adminId != null && !adminId.isEmpty() && !"null".equalsIgnoreCase(adminId) && !"undefined".equalsIgnoreCase(adminId)) {
            try {
                parsedAdminId = Long.parseLong(adminId);
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        return visitService.getStats(month, week, year, parsedAdminId);
    }
}