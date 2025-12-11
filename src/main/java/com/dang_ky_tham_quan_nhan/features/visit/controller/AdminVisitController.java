package com.dang_ky_tham_quan_nhan.features.visit.controller;

import com.dang_ky_tham_quan_nhan.features.visit.dto.UpdateStatusRequest;
import com.dang_ky_tham_quan_nhan.features.visit.service.VisitService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminVisitController {

    private final VisitService visitService;

    public AdminVisitController(VisitService visitService) {
        this.visitService = visitService;
    }

    @GetMapping("/registrations")
    public Map<String, Object> getRegistrations(
            @RequestParam(required = false) Long unitId,
            @RequestParam(required = false) Integer week,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        // Note: Pagination is not fully implemented in the mapper yet for simplicity, 
        // returning full list for now or can use Mybatis Plus IPage if needed. 
        // Just returning list structure as per requirement.
        List<Map<String, Object>> data = visitService.searchAdmin(unitId, week, province, status, keyword);
        return Map.of("data", data, "total", data.size());
    }

    @PutMapping("/registrations/{id}/status")
    public void updateStatus(@PathVariable Long id, @RequestBody UpdateStatusRequest request) {
        visitService.updateStatus(id, request);
    }

    @DeleteMapping("/registrations/{id}")
    public void deleteRegistration(@PathVariable Long id) {
        visitService.deleteRegistration(id);
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats(@RequestParam(required = false) Integer week) {
        return visitService.getStats(week);
    }
}
