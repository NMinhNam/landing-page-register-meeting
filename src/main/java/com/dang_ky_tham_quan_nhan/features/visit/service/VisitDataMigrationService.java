package com.dang_ky_tham_quan_nhan.features.visit.service;

import com.dang_ky_tham_quan_nhan.features.visit.entity.VisitRegistration;
import com.dang_ky_tham_quan_nhan.features.visit.mapper.VisitRegistrationMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VisitDataMigrationService {

    private final VisitRegistrationMapper visitRegistrationMapper;
    private final VisitService visitService;

    public VisitDataMigrationService(VisitRegistrationMapper visitRegistrationMapper, VisitService visitService) {
        this.visitRegistrationMapper = visitRegistrationMapper;
        this.visitService = visitService;
    }

    public void migrateExistingRecords() {
        // Get all existing records
        List<VisitRegistration> allRegistrations = visitRegistrationMapper.findAll();

        for (VisitRegistration registration : allRegistrations) {
            // Calculate the display values using the same logic as in registerVisit
            String weekMonthDisplay = visitService.calculateWeekMonthDisplay(
                registration.getVisitWeek(), 
                registration.getCreatedAt()
            );
            
            // Update the registration with calculated values
            registration.setVisitWeekMonthDisplay(weekMonthDisplay);
            registration.setVisitYear(registration.getCreatedAt().getYear());
            
            // Update in database
            visitRegistrationMapper.update(registration);
        }
    }
}