package com.dang_ky_tham_quan_nhan.features.soldier.controller;

import com.dang_ky_tham_quan_nhan.features.soldier.entity.Soldier;
import com.dang_ky_tham_quan_nhan.features.soldier.service.SoldierService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/soldiers")
public class PublicSoldierController {

    private final SoldierService soldierService;

    public PublicSoldierController(SoldierService soldierService) {
        this.soldierService = soldierService;
    }

    @GetMapping
    public List<Soldier> searchSoldiers(
            @RequestParam(required = false) Long unitId,
            @RequestParam(required = false) String keyword
    ) {
        return soldierService.searchSoldiers(unitId, keyword);
    }
}
