package com.iris.increff.service;

import com.iris.increff.dao.SalesDao;
import com.iris.increff.dao.SkuDao;
import com.iris.increff.dao.StyleDao;
import com.iris.increff.dao.StoreDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for handling data clearing operations with proper dependency management.
 * Ensures foreign key constraints are respected when clearing data for uploads.
 * 
 * Dependency Chain:
 * Sales → SKU, Store (sales references both)
 * SKU → Style (sku references style)
 * 
 * @author Your Name
 * @version 1.0
 * @since 2025-01-01
 */
@Service
public class DataClearingService {

    @Autowired
    private SalesDao salesDao;
    
    @Autowired
    private SkuDao skuDao;
    
    @Autowired
    private StyleDao styleDao;
    
    @Autowired
    private StoreDao storeDao;

    /**
     * Clear data for Style upload - must clear in dependency order
     * Order: Sales (child) → SKUs (middle) → Styles (parent)
     */
    @Transactional
    public void clearDataForStyleUpload() {
        System.out.println("Clearing data for Style upload - handling dependencies...");
        salesDao.deleteAll();  // Clear sales first (depends on SKUs + Stores)
        skuDao.deleteAll();    // Clear SKUs second (depends on Styles)  
        styleDao.deleteAll();  // Now safe to clear Styles
    }

    /**
     * Clear data for Store upload - must clear dependent sales first
     * Order: Sales (child) → Stores (parent)
     */
    @Transactional
    public void clearDataForStoreUpload() {
        System.out.println("Clearing data for Store upload - handling dependencies...");
        salesDao.deleteAll();  // Clear sales first (depends on Stores)
        storeDao.deleteAll();  // Now safe to clear Stores
    }

    /**
     * Clear data for SKU upload - must clear dependent sales first
     * Order: Sales (child) → SKUs (parent)
     */
    @Transactional
    public void clearDataForSkuUpload() {
        System.out.println("Clearing data for SKU upload - handling dependencies...");
        salesDao.deleteAll();  // Clear sales first (depends on SKUs)
        skuDao.deleteAll();    // Now safe to clear SKUs
    }

    /**
     * Clear data for Sales upload - no dependencies to worry about
     */
    @Transactional
    public void clearDataForSalesUpload() {
        System.out.println("Clearing data for Sales upload...");
        salesDao.deleteAll();  // Sales has no children, safe to clear directly
    }

    /**
     * Clear all data in proper dependency order (for testing/reset purposes)
     */
    @Transactional
    public void clearAllData() {
        System.out.println("Clearing all data - handling all dependencies...");
        salesDao.deleteAll();   // Clear deepest children first
        skuDao.deleteAll();     // Clear middle level
        styleDao.deleteAll();   // Clear parents
        storeDao.deleteAll();   // Independent parent
    }
}
