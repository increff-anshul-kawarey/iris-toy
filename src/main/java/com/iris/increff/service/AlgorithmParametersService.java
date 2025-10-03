package com.iris.increff.service;

import com.iris.increff.dao.AlgorithmParametersDao;
import com.iris.increff.model.AlgorithmParameters;
import com.iris.increff.model.AlgoParametersData;
import com.iris.increff.model.AuditLog;
import com.iris.increff.dao.AuditLogDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class AlgorithmParametersService {

    @Autowired
    private AlgorithmParametersDao algorithmParametersDao;

    @Autowired
    private AuditLogDao auditLogDao;

    @Transactional(readOnly = true)
    public List<AlgorithmParameters> getAll() {
        return algorithmParametersDao.findAllActive();
    }

    @Transactional(readOnly = true)
    public AlgorithmParameters get(String parameterSet) {
        return algorithmParametersDao.findByParameterSet(parameterSet);
    }

    @Transactional
    public void add(AlgorithmParameters p) {
        // In a real app, add more validation
        algorithmParametersDao.save(p);
    }

    @Transactional
    public void update(String parameterSet, AlgorithmParameters p) {
        AlgorithmParameters existing = algorithmParametersDao.findByParameterSet(parameterSet);
        if (existing == null) {
            // Or throw an exception
            return;
        }

        StringBuilder details = new StringBuilder();
        // Compare each field and build the audit log details
        addAuditDetail(details, "liquidationThreshold", existing.getLiquidationThreshold(), p.getLiquidationThreshold());
        addAuditDetail(details, "bestsellerMultiplier", existing.getBestsellerMultiplier(), p.getBestsellerMultiplier());
        addAuditDetail(details, "minVolumeThreshold", existing.getMinVolumeThreshold(), p.getMinVolumeThreshold());
        addAuditDetail(details, "consistencyThreshold", existing.getConsistencyThreshold(), p.getConsistencyThreshold());
        addAuditDetail(details, "description", existing.getDescription(), p.getDescription());
        
        // Update the existing entity
        existing.setLiquidationThreshold(p.getLiquidationThreshold());
        existing.setBestsellerMultiplier(p.getBestsellerMultiplier());
        existing.setMinVolumeThreshold(p.getMinVolumeThreshold());
        existing.setConsistencyThreshold(p.getConsistencyThreshold());
        existing.setDescription(p.getDescription());

        if (details.length() > 0) {
            AuditLog log = new AuditLog();
            log.setTimestamp(new java.util.Date());
            log.setEntityType("AlgorithmParameters");
            log.setEntityId(existing.getId().intValue());
            log.setAction("UPDATE");
            log.setDetails(details.toString());
            log.setModifiedBy("system");
            auditLogDao.insert(log);
        }

        algorithmParametersDao.save(existing);
    }

    private void addAuditDetail(StringBuilder details, String fieldName, Object oldValue, Object newValue) {
        if (!Objects.equals(oldValue, newValue)) {
            details.append(String.format("%s changed from '%s' to '%s'; ", fieldName, oldValue, newValue));
        }
    }

    // ===== DTO-based helpers for UI =====

    /**
     * Get current active parameters as DTO. Falls back to defaults.
     */
    @Transactional(readOnly = true)
    public AlgoParametersData getCurrentParameters() {
        List<AlgorithmParameters> active = algorithmParametersDao.findAllActive();
        AlgorithmParameters current = (active != null && !active.isEmpty()) ? active.get(0) : null;
        if (current == null) {
            current = algorithmParametersDao.getDefaultParameters();
        }
        return current != null ? current.toAlgoParametersData() : null;
    }

    /**
     * Get default parameters as DTO (creates row if absent).
     */
    @Transactional(readOnly = true)
    public AlgoParametersData getDefaultParametersAsData() {
        AlgorithmParameters def = algorithmParametersDao.getDefaultParameters();
        return def != null ? def.toAlgoParametersData() : null;
    }

    /**
     * Get a specific parameter set as DTO.
     */
    @Transactional(readOnly = true)
    public AlgoParametersData getParameterSetData(String parameterSet) {
        AlgorithmParameters p = algorithmParametersDao.findByParameterSet(parameterSet);
        return p != null ? p.toAlgoParametersData() : null;
    }

    /**
     * Get all active parameter sets as DTOs.
     */
    @Transactional(readOnly = true)
    public java.util.List<AlgoParametersData> getActiveParameterSetsData() {
        List<AlgorithmParameters> active = algorithmParametersDao.findAllActive();
        java.util.ArrayList<AlgoParametersData> out = new java.util.ArrayList<>();
        if (active != null) {
            for (AlgorithmParameters p : active) {
                out.add(p.toAlgoParametersData());
            }
        }
        return out;
    }

    /**
     * Get recent parameter sets (active and inactive) as DTOs, limited count.
     */
    @Transactional(readOnly = true)
    public java.util.List<AlgoParametersData> getRecentParameterSetsData(int limit) {
        List<AlgorithmParameters> sets = algorithmParametersDao.findRecentSets(limit);
        java.util.ArrayList<AlgoParametersData> out = new java.util.ArrayList<>();
        if (sets != null) {
            for (AlgorithmParameters p : sets) {
                out.add(p.toAlgoParametersData());
            }
        }
        return out;
    }

    /**
     * Create a new parameter set and deactivate previous active one.
     */
    @Transactional
    public AlgoParametersData createNewParameterSet(AlgoParametersData data, String parameterSetName) {
        // Deactivate current active
        List<AlgorithmParameters> active = algorithmParametersDao.findAllActive();
        if (active != null) {
            for (AlgorithmParameters p : active) {
                p.setIsActive(false);
                algorithmParametersDao.save(p);
            }
        }

        AlgorithmParameters entity = new AlgorithmParameters();
        entity.setParameterSet(parameterSetName != null ? parameterSetName : "run_" + System.currentTimeMillis());
        entity.setIsActive(true);
        entity.updateFromAlgoParametersData(data, "system");
        algorithmParametersDao.save(entity);
        return entity.toAlgoParametersData();
    }

    /**
     * Update current (or default) parameters from DTO and return updated DTO.
     */
    @Transactional
    public AlgoParametersData updateCurrentParameters(AlgoParametersData data) {
        List<AlgorithmParameters> active = algorithmParametersDao.findAllActive();
        AlgorithmParameters current = (active != null && !active.isEmpty()) ? active.get(0) : null;
        if (current == null) {
            current = algorithmParametersDao.getDefaultParameters();
        }
        if (current == null) {
            current = new AlgorithmParameters();
            current.setParameterSet("default");
            current.setIsActive(true);
        }
        current.updateFromAlgoParametersData(data, "system");
        algorithmParametersDao.save(current);
        return current.toAlgoParametersData();
    }
}
