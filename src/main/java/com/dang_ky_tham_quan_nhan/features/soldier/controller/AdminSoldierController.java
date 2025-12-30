package com.dang_ky_tham_quan_nhan.features.soldier.controller;

import com.dang_ky_tham_quan_nhan.features.soldier.dto.SoldierRequest;
import com.dang_ky_tham_quan_nhan.features.soldier.entity.Soldier;
import com.dang_ky_tham_quan_nhan.features.soldier.service.SoldierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/soldiers")
@Tag(name = "Admin Soldier", description = "Soldier Management APIs for Admins")
public class AdminSoldierController {

    private final SoldierService soldierService;

    public AdminSoldierController(SoldierService soldierService) {
        this.soldierService = soldierService;
    }

    @GetMapping
    @Operation(summary = "Search Soldiers", description = "Search soldiers by unit ID or keyword.")
    public List<Soldier> searchSoldiers(
            @RequestParam(required = false) Long unitId,
            @RequestParam(required = false) String keyword
    ) {
        return soldierService.searchSoldiers(unitId, keyword);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Soldier by ID", description = "Retrieve detailed information of a soldier.")
    public Soldier getSoldierById(@PathVariable Long id) {
        return soldierService.getSoldierById(id);
    }

    @PostMapping
    @Operation(summary = "Create Soldier", description = "Create a new soldier record.")
    public void createSoldier(@RequestBody SoldierRequest request) {
        soldierService.createSoldier(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Soldier", description = "Update existing soldier information.")
    public void updateSoldier(@PathVariable Long id, @RequestBody SoldierRequest request) {
        soldierService.updateSoldier(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Soldier", description = "Delete a soldier record.")
    public void deleteSoldier(@PathVariable Long id) {
        soldierService.deleteSoldier(id);
    }
}