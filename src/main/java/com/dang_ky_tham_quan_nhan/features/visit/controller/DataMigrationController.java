package com.dang_ky_tham_quan_nhan.features.visit.controller;

import com.dang_ky_tham_quan_nhan.features.visit.service.VisitDataMigrationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/migration")
public class DataMigrationController {

    private final VisitDataMigrationService migrationService;

    public DataMigrationController(VisitDataMigrationService migrationService) {
        this.migrationService = migrationService;
    }

    @PostMapping("/migrate-existing-records")
    public String migrateExistingRecords() {
        try {
            migrationService.migrateExistingRecords();
            return "Migration completed successfully";
        } catch (Exception e) {
            return "Migration failed: " + e.getMessage();
        }
    }
}