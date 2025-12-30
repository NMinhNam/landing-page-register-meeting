package com.dang_ky_tham_quan_nhan.features.soldier.controller;

import com.dang_ky_tham_quan_nhan.features.soldier.entity.Soldier;
import com.dang_ky_tham_quan_nhan.features.soldier.service.SoldierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/soldiers")
@Tag(name = "Public Soldier", description = "Public Soldier Search APIs")
public class PublicSoldierController {

    private final SoldierService soldierService;

    public PublicSoldierController(SoldierService soldierService) {
        this.soldierService = soldierService;
    }

    @GetMapping
    @Operation(summary = "Search Soldiers (Public)", description = "Allow relatives to search for soldiers to register.")
    public List<Soldier> searchSoldiers(
            @RequestParam(required = false) Long unitId,
            @RequestParam(required = false) String keyword
    ) {
        return soldierService.searchSoldiers(unitId, keyword);
    }
}