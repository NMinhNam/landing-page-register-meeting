package com.dang_ky_tham_quan_nhan.features.visit.controller;

import com.dang_ky_tham_quan_nhan.features.visit.dto.RegistrationRequest;
import com.dang_ky_tham_quan_nhan.features.visit.entity.VisitRegistration;
import com.dang_ky_tham_quan_nhan.features.visit.service.VisitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/public/registrations")
@Tag(name = "Public Visit", description = "Public Visit Registration APIs")
public class PublicVisitController {

    private final VisitService visitService;

    public PublicVisitController(VisitService visitService) {
        this.visitService = visitService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register Visit", description = "Submit a new visit registration request.")
    public Map<String, Object> register(@RequestBody RegistrationRequest request) {
        VisitRegistration reg = visitService.registerVisit(request);
        Map<String, Object> response = new HashMap<>();
        response.put("registrationCode", "REG-" + reg.getId());
        response.put("message", "Đăng ký thành công, vui lòng chờ duyệt.");
        return response;
    }

    @GetMapping("/search")
    @Operation(summary = "Search Registrations (Public)", description = "Check status of registration by phone or code.")
    public List<Map<String, Object>> search(@RequestParam(required = false) String phone,
                                            @RequestParam(required = false) String code) {
        List<Map<String, Object>> results = visitService.searchRegistration(phone, code);
        
        return results.stream().map(row -> {
            Map<String, Object> response = new HashMap<>();
            response.put("id", row.get("id"));
            response.put("soldierName", row.get("soldier_name"));
            response.put("unitName", row.get("unit_name"));
            response.put("relativeName", row.get("relative_name")); // GROUP_CONCAT
            response.put("visitWeek", row.get("visit_week"));
            response.put("status", row.get("status"));
            response.put("note", row.get("note"));
            response.put("createdAt", row.get("created_at"));
            return response;
        }).toList();
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel Registration", description = "Cancel a pending registration.")
    public void cancel(@PathVariable Long id) {
        visitService.cancelRegistration(id);
    }
}