package com.iris.increff.dao;

import com.iris.increff.AbstractUnitTest;
import com.iris.increff.model.AuditLog;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test class for AuditLogDao
 * Tests audit log persistence and retrieval operations
 */
public class AuditLogDaoTest extends AbstractUnitTest {

    @Autowired
    private AuditLogDao auditLogDao;

    @Test
    public void testInsert() throws Exception {
        AuditLog log = new AuditLog();
        log.setTimestamp(new Date());
        log.setEntityType("Style");
        log.setEntityId(1);
        log.setAction("CREATE");
        log.setDetails("Created new style");
        log.setModifiedBy("admin");

        auditLogDao.insert(log);

        assertNotNull(log.getId());
        assertTrue(log.getId() > 0);
    }

    @Test
    public void testSelectAll() throws Exception {
        // Insert test logs
        AuditLog log1 = createTestAuditLog("Style", 1, "CREATE");
        auditLogDao.insert(log1);

        AuditLog log2 = createTestAuditLog("SKU", 2, "UPDATE");
        auditLogDao.insert(log2);

        // Test selectAll
        List<AuditLog> logs = auditLogDao.selectAll();
        assertNotNull(logs);
        assertTrue(logs.size() >= 2);
    }

    @Test
    public void testInsertMultipleLogs() throws Exception {
        int initialCount = auditLogDao.selectAll().size();

        // Insert multiple logs
        for (int i = 0; i < 5; i++) {
            AuditLog log = createTestAuditLog("TestEntity", i, "TEST_ACTION_" + i);
            auditLogDao.insert(log);
        }

        List<AuditLog> logs = auditLogDao.selectAll();
        assertTrue(logs.size() >= initialCount + 5);
    }

    @Test
    public void testInsertWithDifferentActions() throws Exception {
        String[] actions = {"CREATE", "UPDATE", "DELETE", "UPLOAD", "DOWNLOAD"};

        for (String action : actions) {
            AuditLog log = createTestAuditLog("TestEntity", 100, action);
            auditLogDao.insert(log);
            assertNotNull(log.getId());
        }

        List<AuditLog> logs = auditLogDao.selectAll();
        assertTrue(logs.size() >= actions.length);
    }

    @Test
    public void testInsertWithDifferentEntityTypes() throws Exception {
        String[] entityTypes = {"Style", "SKU", "Sales", "Store", "AlgorithmParameters", "Task"};

        for (int i = 0; i < entityTypes.length; i++) {
            AuditLog log = createTestAuditLog(entityTypes[i], i, "TEST");
            auditLogDao.insert(log);
            assertNotNull(log.getId());
        }

        List<AuditLog> logs = auditLogDao.selectAll();
        assertTrue(logs.size() >= entityTypes.length);
    }

    @Test
    public void testInsertWithLongDetails() throws Exception {
        AuditLog log = new AuditLog();
        log.setTimestamp(new Date());
        log.setEntityType("Style");
        log.setEntityId(1);
        log.setAction("UPDATE");

        // Create long details string
        StringBuilder longDetails = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            longDetails.append("Field").append(i).append(" changed from value").append(i)
                      .append(" to newValue").append(i).append("; ");
        }
        log.setDetails(longDetails.toString());
        log.setModifiedBy("testUser");

        auditLogDao.insert(log);
        assertNotNull(log.getId());

        // Verify it was saved
        List<AuditLog> logs = auditLogDao.selectAll();
        boolean found = false;
        for (AuditLog retrieved : logs) {
            if (retrieved.getId() == log.getId()) {
                found = true;
                assertNotNull(retrieved.getDetails());
                assertTrue(retrieved.getDetails().length() > 100);
                break;
            }
        }
        assertTrue(found);
    }

    @Test
    public void testInsertWithNullOptionalFields() throws Exception {
        AuditLog log = new AuditLog();
        log.setTimestamp(new Date());
        log.setEntityType("TestEntity");
        log.setEntityId(1);
        log.setAction("TEST");
        log.setDetails(null); // Null details
        log.setModifiedBy("testUser"); // Set required field

        auditLogDao.insert(log);
        assertNotNull(log.getId());
    }

    @Test
    public void testAuditTrailForAlgorithmParameters() throws Exception {
        // Simulate audit trail for algorithm parameter updates
        AuditLog log = new AuditLog();
        log.setTimestamp(new Date());
        log.setEntityType("AlgorithmParameters");
        log.setEntityId(1);
        log.setAction("UPDATE");
        log.setDetails("liquidationThreshold changed from '0.25' to '0.30'; " +
                      "bestsellerMultiplier changed from '1.20' to '1.40'");
        log.setModifiedBy("admin");

        auditLogDao.insert(log);
        assertNotNull(log.getId());

        List<AuditLog> logs = auditLogDao.selectAll();
        boolean found = false;
        for (AuditLog retrieved : logs) {
            if (retrieved.getId() == log.getId()) {
                found = true;
                assertEquals("AlgorithmParameters", retrieved.getEntityType());
                assertEquals("UPDATE", retrieved.getAction());
                assertTrue(retrieved.getDetails().contains("liquidationThreshold"));
                break;
            }
        }
        assertTrue(found);
    }

    // Helper method to create test audit log
    private AuditLog createTestAuditLog(String entityType, Integer entityId, String action) {
        AuditLog log = new AuditLog();
        log.setTimestamp(new Date());
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setAction(action);
        log.setDetails("Test audit log details");
        log.setModifiedBy("testUser");
        return log;
    }
}

