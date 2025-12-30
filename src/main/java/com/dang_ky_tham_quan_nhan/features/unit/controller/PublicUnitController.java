package com.dang_ky_tham_quan_nhan.features.unit.controller;

import com.dang_ky_tham_quan_nhan.features.unit.entity.Unit;
import com.dang_ky_tham_quan_nhan.features.unit.service.UnitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/units")
@Tag(name = "Public Unit", description = "Public Unit APIs for registration")
public class PublicUnitController {

    private final UnitService unitService;

    public PublicUnitController(UnitService unitService) {
        this.unitService = unitService;
    }

    @GetMapping
    @Operation(summary = "Get All Units for Public", description = "Retrieve a list of all military units for registration form.")
    public List<Unit> getAllUnits() {
        return unitService.getAllUnits();
    }
}
