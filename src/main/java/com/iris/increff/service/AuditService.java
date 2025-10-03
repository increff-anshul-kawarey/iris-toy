package com.iris.increff.service;

import com.iris.increff.dao.AuditLogDao;
import com.iris.increff.model.AuditLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * Service for creating audit log entries.
 * Tracks all data modifications for compliance and history.
 * 
 * @author Your Name
 * @version 1.0
 * @since 2025-01-02
 */
@Service
public class AuditService {

    @Autowired
    private AuditLogDao auditLogDao;

    /**
     * Log a data modification action.
     * Uses REQUIRES_NEW propagation to ensure audit logs are saved even if parent transaction rolls back.
     * 
     * @param entityType Type of entity (e.g., "Style", "SKU")
     * @param entityId ID of the entity (0 for bulk operations)
     * @param action Action performed (e.g., "INSERT", "UPDATE", "DELETE", "BULK_DELETE")
     * @param details Human-readable description of what changed
     * @param modifiedBy Who made the change (user ID or "system")
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(String entityType, int entityId, String action, String details, String modifiedBy) {
        AuditLog log = new AuditLog();
        log.setTimestamp(new Date());
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setAction(action);
        log.setDetails(details);
        log.setModifiedBy(modifiedBy);
        
        auditLogDao.insert(log);
    }

    /**
     * Log a bulk operation (multiple records affected).
     * 
     * @param entityType Type of entities affected
     * @param action Action performed
     * @param count Number of records affected
     * @param details Additional details
     * @param modifiedBy Who made the change
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logBulkAction(String entityType, String action, int count, String details, String modifiedBy) {
        AuditLog log = new AuditLog();
        log.setTimestamp(new Date());
        log.setEntityType(entityType);
        log.setEntityId(0); // 0 indicates bulk operation
        log.setAction(action);
        log.setDetails(String.format("%s (%d records): %s", action, count, details));
        log.setModifiedBy(modifiedBy);
        
        auditLogDao.insert(log);
    }
}

