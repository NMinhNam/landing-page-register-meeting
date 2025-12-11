package com.dang_ky_tham_quan_nhan.features.soldier.service;

import com.dang_ky_tham_quan_nhan.features.soldier.dto.SoldierRequest;
import com.dang_ky_tham_quan_nhan.features.soldier.entity.Soldier;
import com.dang_ky_tham_quan_nhan.features.soldier.mapper.SoldierMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SoldierService {

    private final SoldierMapper soldierMapper;

    public SoldierService(SoldierMapper soldierMapper) {
        this.soldierMapper = soldierMapper;
    }
    
    public List<Soldier> searchSoldiers(Long unitId, String keyword) {
        return soldierMapper.searchSoldiers(unitId, keyword);
    }

    public Soldier getSoldierById(Long id) {
        return soldierMapper.findById(id);
    }

    public void createSoldier(SoldierRequest request) {
        Soldier soldier = new Soldier();
        soldier.setCode(request.getCode());
        soldier.setName(request.getName());
        soldier.setUnitId(request.getUnitId());
        soldier.setStatus(request.getStatus());
        soldierMapper.insert(soldier);
    }

    public void updateSoldier(Long id, SoldierRequest request) {
        Soldier soldier = soldierMapper.findById(id);
        if (soldier != null) {
            soldier.setCode(request.getCode());
            soldier.setName(request.getName());
            soldier.setUnitId(request.getUnitId());
            soldier.setStatus(request.getStatus());
            soldierMapper.update(soldier);
        }
    }

    public void deleteSoldier(Long id) {
        soldierMapper.delete(id);
    }
}