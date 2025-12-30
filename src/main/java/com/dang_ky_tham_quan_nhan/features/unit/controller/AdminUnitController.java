package com.dang_ky_tham_quan_nhan.features.unit.controller;

import com.dang_ky_tham_quan_nhan.features.unit.dto.UnitRequest;
import com.dang_ky_tham_quan_nhan.features.unit.entity.Unit;
import com.dang_ky_tham_quan_nhan.features.unit.service.UnitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/units")
@Tag(name = "Admin Unit", description = "Unit Management APIs")
public class AdminUnitController {

    private final UnitService unitService;

    public AdminUnitController(UnitService unitService) {
        this.unitService = unitService;
    }

    @GetMapping
    @Operation(summary = "Get All Units", description = "Retrieve a list of all military units.")
    public List<Unit> getAllUnits() {
        return unitService.getAllUnits();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Unit by ID", description = "Retrieve details of a specific unit.")
    public Unit getUnitById(@PathVariable Long id) {
        return unitService.getUnitById(id);
    }

    @PostMapping
    @Operation(summary = "Create Unit", description = "Create a new unit.")
    public void createUnit(@RequestBody UnitRequest request) {
        unitService.createUnit(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Unit", description = "Update an existing unit.")
    public void updateUnit(@PathVariable Long id, @RequestBody UnitRequest request) {
        unitService.updateUnit(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Unit", description = "Delete a unit.")
    public void deleteUnit(@PathVariable Long id) {
        unitService.deleteUnit(id);
    }
}