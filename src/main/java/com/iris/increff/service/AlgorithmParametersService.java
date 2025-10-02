package com.iris.increff.service;

import com.iris.increff.dao.AlgorithmParametersDao;
import com.iris.increff.model.AlgorithmParameters;
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
}
