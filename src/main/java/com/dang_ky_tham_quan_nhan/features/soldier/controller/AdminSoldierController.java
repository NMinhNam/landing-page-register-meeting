package com.dang_ky_tham_quan_nhan.features.soldier.controller;

import com.dang_ky_tham_quan_nhan.features.soldier.dto.SoldierRequest;
import com.dang_ky_tham_quan_nhan.features.soldier.entity.Soldier;
import com.dang_ky_tham_quan_nhan.features.soldier.service.SoldierService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/soldiers")
public class AdminSoldierController {

    private final SoldierService soldierService;

    public AdminSoldierController(SoldierService soldierService) {
        this.soldierService = soldierService;
    }

    @GetMapping
    public List<Soldier> searchSoldiers(
            @RequestParam(required = false) Long unitId,
            @RequestParam(required = false) String keyword
    ) {
        return soldierService.searchSoldiers(unitId, keyword);
    }

    @GetMapping("/{id}")
    public Soldier getSoldierById(@PathVariable Long id) {
        return soldierService.getSoldierById(id);
    }

    @PostMapping
    public void createSoldier(@RequestBody SoldierRequest request) {
        soldierService.createSoldier(request);
    }

    @PutMapping("/{id}")
    public void updateSoldier(@PathVariable Long id, @RequestBody SoldierRequest request) {
        soldierService.updateSoldier(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteSoldier(@PathVariable Long id) {
        soldierService.deleteSoldier(id);
    }
}
