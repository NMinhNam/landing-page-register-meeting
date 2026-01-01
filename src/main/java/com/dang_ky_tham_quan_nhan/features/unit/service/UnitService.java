package com.dang_ky_tham_quan_nhan.features.unit.service;

import com.dang_ky_tham_quan_nhan.features.unit.dto.UnitRequest;
import com.dang_ky_tham_quan_nhan.features.unit.entity.Unit;
import com.dang_ky_tham_quan_nhan.features.unit.mapper.UnitMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UnitService {
    
    private final UnitMapper unitMapper;
    
    public UnitService(UnitMapper unitMapper) {
        this.unitMapper = unitMapper;
    }

    /**
     * Get list of unit IDs including the parent and all its descendants
     */
    public List<Long> getAllChildUnitIds(Long parentId) {
        List<Unit> allUnits = unitMapper.findAll();
        List<Long> resultIds = new ArrayList<>();
        
        // Add self
        resultIds.add(parentId);
        
        // Add children recursively
        findChildren(parentId, allUnits, resultIds);
        
        return resultIds;
    }

    private void findChildren(Long parentId, List<Unit> allUnits, List<Long> resultIds) {
        List<Unit> children = allUnits.stream()
                .filter(u -> parentId.equals(u.getParentId()))
                .collect(Collectors.toList());
        
        for (Unit child : children) {
            resultIds.add(child.getId());
            findChildren(child.getId(), allUnits, resultIds);
        }
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