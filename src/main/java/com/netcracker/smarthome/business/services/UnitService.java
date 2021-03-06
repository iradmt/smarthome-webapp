package com.netcracker.smarthome.business.services;

import com.netcracker.smarthome.dal.repositories.UnitRepository;
import com.netcracker.smarthome.model.entities.Unit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;

@Service
public class UnitService {
    private final UnitRepository unitRepository;
    
    @Autowired
    public UnitService(UnitRepository unitRepository) {
        this.unitRepository = unitRepository;
    }
    
    @Transactional(readOnly = true)
    public List<Unit> getUnits() {
        return unitRepository.getUnits();
    } 
    
    @Transactional
    public void updateUnit(Unit unit) {
        unitRepository.update(unit);
    }

    @Transactional(readOnly = true)
    public Unit getUnit(Object primaryKey) {
        return unitRepository.get(primaryKey);
    }

    @Transactional
    public void createUnit(Unit unit) {
        unitRepository.save(unit);
    }

    @Transactional(readOnly = true)
    public boolean checkUnitName(String unitName) {
        return unitRepository.checkUnitName(unitName);
    }

    @Transactional
    public void deleteUnit(Object primaryKey) {
        unitRepository.delete(primaryKey);
    }

    @Transactional(readOnly = true)
    public List<BigInteger> getBaseFactors() {
        return unitRepository.getBaseFactors();
    }
}
