package com.iris.increff.service;

import com.iris.increff.dao.SalesDao;
import com.iris.increff.dao.SkuDao;
import com.iris.increff.dao.StyleDao;
import com.iris.increff.dao.StoreDao;
import com.iris.increff.dao.TaskDao;
import com.iris.increff.dao.NoosResultDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for handling data clearing operations with proper dependency management.
 * Ensures foreign key constraints are respected when clearing data.
 * 
 * Usage Pattern (Updated):
 * - Style/SKU/Store uploads now use UPSERT logic (no clearing needed during normal uploads)
 * - Sales uploads use TRUNCATE (clearDataForSalesUpload) for complete replacement
 * - clearAllData() available for explicit "fresh start" scenarios
 * 
 * Dependency Chain:
 * Sales → SKU, Store (sales references both)
 * SKU → Style (sku references style)
 * 
 * @author Your Name
 * @version 2.0
 * @since 2025-01-01
 */
@Service
public class DataClearingService {

    private static final Logger logger = LoggerFactory.getLogger(DataClearingService.class);

    @Autowired
    private SalesDao salesDao;
    
    @Autowired
    private SkuDao skuDao;
    
    @Autowired
    private StyleDao styleDao;
    
    @Autowired
    private StoreDao storeDao;

    @Autowired
    private TaskDao taskDao;

    @Autowired
    private NoosResultDao noosResultDao;

    @Autowired
    private AuditService auditService;

    @javax.persistence.PersistenceContext
    private javax.persistence.EntityManager entityManager;

    /**
     * Clear data for Style upload - must clear in dependency order
     * Order: Sales (child) → SKUs (middle) → Styles (parent)
     * 
     * NOTE: This method is kept for backwards compatibility and "Clear All" functionality.
     * Normal style uploads now use UPSERT logic and don't call this method.
     */
    @Transactional
    public void clearDataForStyleUpload() {
        logger.info("Clearing data for Style upload - handling dependencies...");
        salesDao.deleteAll();  // Clear sales first (depends on SKUs + Stores)
        skuDao.deleteAll();    // Clear SKUs second (depends on Styles)  
        styleDao.deleteAll();  // Now safe to clear Styles
    }

    /**
     * Clear data for Store upload - must clear dependent sales first
     * Order: Sales (child) → Stores (parent)
     * 
     * NOTE: This method is kept for backwards compatibility and "Clear All" functionality.
     * Normal store uploads now use UPSERT logic and don't call this method.
     */
    @Transactional
    public void clearDataForStoreUpload() {
        logger.info("Clearing data for Store upload - handling dependencies...");
        salesDao.deleteAll();  // Clear sales first (depends on Stores)
        storeDao.deleteAll();  // Now safe to clear Stores
    }

    /**
     * Clear data for SKU upload - must clear dependent sales first
     * Order: Sales (child) → SKUs (parent)
     * 
     * NOTE: This method is kept for backwards compatibility and "Clear All" functionality.
     * Normal SKU uploads now use UPSERT logic and don't call this method.
     */
    @Transactional
    public void clearDataForSkuUpload() {
        logger.info("Clearing data for SKU upload - handling dependencies...");
        salesDao.deleteAll();  // Clear sales first (depends on SKUs)
        skuDao.deleteAll();    // Now safe to clear SKUs
    }

    /**
     * Clear data for Sales upload - no dependencies to worry about
     * 
     * This method is actively used for sales uploads (TRUNCATE mode).
     * Sales data uses complete replacement rather than UPSERT for simplicity.
     */
    @Transactional
    public void clearDataForSalesUpload() {
        logger.info("Clearing data for Sales upload...");
        salesDao.deleteAll();  // Sales has no children, safe to clear directly
    }

    /**
     * Clear all data in proper dependency order.
     * Use this for explicit "fresh start" scenarios when you want to completely
     * reset the database before loading a new complete dataset.
     * 
     * This should be triggered via explicit user action (e.g., "Clear All Data" button)
     * rather than automatically during uploads.
     */
    @Transactional
    public void clearAllData() {
        logger.warn("Clearing all data - handling all dependencies...");
        
        // Get counts before deletion for audit log
        Long salesCount = salesDao.getTotalSalesCount();
        Long skuCount = skuDao.getTotalSkuCount();
        Long styleCount = styleDao.getTotalStyleCount();
        Long storeCount = storeDao.getTotalStoreCount();
        Long taskCount = taskDao.getCount();
        Long noosResultCount = noosResultDao.getCount();
        
        salesDao.deleteAll();      // Clear deepest children first
        skuDao.deleteAll();        // Clear middle level
        styleDao.deleteAll();      // Clear parents
        storeDao.deleteAll();      // Independent parent
        taskDao.deleteAll();       // Clear tasks
        noosResultDao.deleteAll(); // Clear NOOS results

        // Reset auto-increment sequences for clean IDs across test runs
        try {
            resetAutoIncrement("sales");
            resetAutoIncrement("skus");
            resetAutoIncrement("styles");
            resetAutoIncrement("stores");
            resetAutoIncrement("tasks");
            resetAutoIncrement("noos_results");
        } catch (Exception e) {
            logger.warn("Failed to reset auto-increment counters: {}", e.getMessage());
        }
        
        // Audit log the clear all operation
        String details = String.format("Cleared all data: %d sales, %d SKUs, %d styles, %d stores, %d tasks, %d NOOS results", 
            salesCount, skuCount, styleCount, storeCount, taskCount, noosResultCount);
        auditService.logBulkAction("System", "CLEAR_ALL_DATA", 
            (int)(salesCount + skuCount + styleCount + storeCount + taskCount + noosResultCount), details, "system");
    }

    /**
     * Reset auto-increment/identity for given table in a database-agnostic manner where possible.
     * Supports H2 (test) and MySQL modes.
     */
    private void resetAutoIncrement(String tableName) {
        // MySQL fallback
        try {
            entityManager.createNativeQuery("ALTER TABLE " + tableName + " AUTO_INCREMENT = 1").executeUpdate();
        } catch (Exception ignored) { }
        
        // H2 in-memory (test profile)
        try {
            entityManager.createNativeQuery("ALTER TABLE " + tableName + " ALTER COLUMN id RESTART WITH 1").executeUpdate();
            return;
        } catch (Exception ignored) { }

        
    }
}
