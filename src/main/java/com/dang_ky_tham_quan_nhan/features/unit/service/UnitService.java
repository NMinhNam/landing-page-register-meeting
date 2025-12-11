package com.dang_ky_tham_quan_nhan.features.unit.service;

import com.dang_ky_tham_quan_nhan.features.unit.dto.UnitRequest;
import com.dang_ky_tham_quan_nhan.features.unit.entity.Unit;
import com.dang_ky_tham_quan_nhan.features.unit.mapper.UnitMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UnitService {
    
    private final UnitMapper unitMapper;
    
    public UnitService(UnitMapper unitMapper) {
        this.unitMapper = unitMapper;
    }
    
    public List<Unit> getAllUnits() {
        return unitMapper.findAll();
    }

    public Unit getUnitById(Long id) {
        return unitMapper.findById(id);
    }

    public void createUnit(UnitRequest request) {
        Unit unit = new Unit();
        unit.setName(request.getName());
        unit.setParentId(request.getParentId());
        unitMapper.insert(unit);
    }

    public void updateUnit(Long id, UnitRequest request) {
        Unit unit = unitMapper.findById(id);
        if (unit != null) {
            unit.setName(request.getName());
            unit.setParentId(request.getParentId());
            unitMapper.update(unit);
        }
    }

    public void deleteUnit(Long id) {
        unitMapper.delete(id);
    }
}