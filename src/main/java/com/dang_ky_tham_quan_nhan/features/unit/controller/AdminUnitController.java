package com.dang_ky_tham_quan_nhan.features.unit.controller;

import com.dang_ky_tham_quan_nhan.features.unit.dto.UnitRequest;
import com.dang_ky_tham_quan_nhan.features.unit.entity.Unit;
import com.dang_ky_tham_quan_nhan.features.unit.service.UnitService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/units")
public class AdminUnitController {

    private final UnitService unitService;

    public AdminUnitController(UnitService unitService) {
        this.unitService = unitService;
    }

    @GetMapping
    public List<Unit> getAllUnits() {
        return unitService.getAllUnits();
    }

    @GetMapping("/{id}")
    public Unit getUnitById(@PathVariable Long id) {
        return unitService.getUnitById(id);
    }

    @PostMapping
    public void createUnit(@RequestBody UnitRequest request) {
        unitService.createUnit(request);
    }

    @PutMapping("/{id}")
    public void updateUnit(@PathVariable Long id, @RequestBody UnitRequest request) {
        unitService.updateUnit(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteUnit(@PathVariable Long id) {
        unitService.deleteUnit(id);
    }
}
