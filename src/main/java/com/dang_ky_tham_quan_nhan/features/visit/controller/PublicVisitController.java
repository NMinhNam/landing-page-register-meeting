package com.dang_ky_tham_quan_nhan.features.visit.controller;

import com.dang_ky_tham_quan_nhan.features.visit.dto.RegistrationRequest;
import com.dang_ky_tham_quan_nhan.features.visit.entity.VisitRegistration;
import com.dang_ky_tham_quan_nhan.features.visit.service.VisitService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/public/registrations")
public class PublicVisitController {

    private final VisitService visitService;

    public PublicVisitController(VisitService visitService) {
        this.visitService = visitService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> register(@RequestBody RegistrationRequest request) {
        VisitRegistration reg = visitService.registerVisit(request);
        Map<String, Object> response = new HashMap<>();
        response.put("registrationCode", "REG-" + reg.getId());
        response.put("message", "Đăng ký thành công, vui lòng chờ duyệt.");
        return response;
    }

    @GetMapping("/search")
    public List<Map<String, Object>> search(@RequestParam(required = false) String phone,
                                            @RequestParam(required = false) String code) {
        List<Map<String, Object>> results = visitService.searchRegistration(phone, code);
        // We can return the raw list directly, or map it if needed.
        // The mapper already returns what we need (including relative_name group_concat).
        // If we want to transform keys (e.g. created_at -> createdAt), we can do it here, 
        // but for simplicity and frontend compatibility let's just return the list 
        // and ensure frontend uses correct keys (snake_case from DB map or adjust here).
        
        // However, the original controller manually mapped keys:
        // response.put("soldierName", first.get("soldier_name"));
        // This implies the frontend expects camelCase.
        
        // Let's map all results to camelCase.
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
    public void cancel(@PathVariable Long id) {
        visitService.cancelRegistration(id);
    }
}
