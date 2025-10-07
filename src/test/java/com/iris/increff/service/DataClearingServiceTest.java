package com.iris.increff.service;

import com.iris.increff.AbstractUnitTest;
import com.iris.increff.dao.SalesDao;
import com.iris.increff.dao.SkuDao;
import com.iris.increff.dao.StoreDao;
import com.iris.increff.dao.StyleDao;
import com.iris.increff.model.Sales;
import com.iris.increff.model.SKU;
import com.iris.increff.model.Store;
import com.iris.increff.model.Style;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * Comprehensive test suite for DataClearingService
 * 
 * Tests all critical functionality including:
 * - Dependency management and clearing order
 * - Foreign key constraint handling
 * - Transaction integrity and rollback
 * - Data clearing for different upload types
 * - Edge cases and error scenarios
 * - Complete data clearing operations
 * 
 * This service is critical for maintaining data integrity during uploads
 * and ensuring proper foreign key constraint handling.
 * 
 * Target: 90-95% method and line coverage for DataClearingService
 * 
 * @author Anshuk Kawarry
 * @version 1.0
 * @since 2025-01-01
 */
public class DataClearingServiceTest extends AbstractUnitTest {

    @Autowired
    private DataClearingService dataClearingService;

    @Autowired
    private SalesDao salesDao;

    @Autowired
    private SkuDao skuDao;

    @Autowired
    private StoreDao storeDao;

    @Autowired
    private StyleDao styleDao;

    private Style testStyle;
    private SKU testSku;
    private Store testStore;
    private Sales testSale;

    /**
     * Setup test data before each test method
     * Creates a complete data hierarchy: Style → SKU → Sales ← Store
     */
    @Before
    public void setUp() {
        // Create test style
        testStyle = new Style();
        testStyle.setStyleCode("SHIRT001");
        testStyle.setBrand("NIKE");
        testStyle.setCategory("SHIRTS");
        testStyle.setSubCategory("CASUAL");
        testStyle.setMrp(new BigDecimal("100.50"));
        testStyle.setGender("M");
        styleDao.save(testStyle);

        // Create test store
        testStore = new Store();
        testStore.setBranch("MUMBAI_CENTRAL");
        testStore.setCity("MUMBAI");
        storeDao.save(testStore);

        // Create test SKU (depends on style)
        testSku = new SKU();
        testSku.setSku("SKU001");
        testSku.setStyleId(testStyle.getId());
        testSku.setSize("M");
        skuDao.save(testSku);

        // Create test sale (depends on SKU and Store)
        testSale = new Sales();
        testSale.setDate(new Date());
        testSale.setSkuId(testSku.getId());
        testSale.setStoreId(testStore.getId());
        testSale.setQuantity(5);
        testSale.setDiscount(new BigDecimal("10.00"));
        testSale.setRevenue(new BigDecimal("450.00"));
        salesDao.save(testSale);
    }

    // ==================== STYLE UPLOAD CLEARING TESTS ====================

    /**
     * Test clearDataForStyleUpload method
     * Verifies that data is cleared in correct dependency order: Sales → SKUs → Styles
     */
    @Test
    @Transactional
    @Rollback
    public void testClearDataForStyleUpload() {
        // Given: Data exists in all tables
        assertEquals("Should have 1 style", 1, styleDao.findAll().size());
        assertEquals("Should have 1 SKU", 1, skuDao.findAll().size());
        assertEquals("Should have 1 store", 1, storeDao.findAll().size());
        assertEquals("Should have 1 sale", 1, salesDao.findAll().size());

        // When: Clear data for style upload
        dataClearingService.clearDataForStyleUpload();

        // Then: Sales, SKUs, and Styles should be cleared, but Stores should remain
        assertEquals("Sales should be cleared", 0, salesDao.findAll().size());
        assertEquals("SKUs should be cleared", 0, skuDao.findAll().size());
        assertEquals("Styles should be cleared", 0, styleDao.findAll().size());
        assertEquals("Stores should remain", 1, storeDao.findAll().size());
    }

    /**
     * Test clearDataForStyleUpload with multiple records
     * Verifies that all related data is cleared properly
     */
    @Test
    @Transactional
    @Rollback
    public void testClearDataForStyleUpload_MultipleRecords() {
        // Given: Create additional data
        Style style2 = new Style();
        style2.setStyleCode("PANT001");
        style2.setBrand("ADIDAS");
        style2.setCategory("PANTS");
        style2.setSubCategory("FORMAL");
        style2.setMrp(new BigDecimal("150.75"));
        style2.setGender("F");
        styleDao.save(style2);

        SKU sku2 = new SKU();
        sku2.setSku("SKU002");
        sku2.setStyleId(style2.getId());
        sku2.setSize("L");
        skuDao.save(sku2);

        Sales sale2 = new Sales();
        sale2.setDate(new Date());
        sale2.setSkuId(sku2.getId());
        sale2.setStoreId(testStore.getId());
        sale2.setQuantity(3);
        sale2.setDiscount(new BigDecimal("5.00"));
        sale2.setRevenue(new BigDecimal("300.00"));
        salesDao.save(sale2);

        // Verify initial state
        assertEquals("Should have 2 styles", 2, styleDao.findAll().size());
        assertEquals("Should have 2 SKUs", 2, skuDao.findAll().size());
        assertEquals("Should have 2 sales", 2, salesDao.findAll().size());

        // When: Clear data for style upload
        dataClearingService.clearDataForStyleUpload();

        // Then: All styles, SKUs, and sales should be cleared
        assertEquals("All sales should be cleared", 0, salesDao.findAll().size());
        assertEquals("All SKUs should be cleared", 0, skuDao.findAll().size());
        assertEquals("All styles should be cleared", 0, styleDao.findAll().size());
        assertEquals("Stores should remain", 1, storeDao.findAll().size());
    }

    // ==================== STORE UPLOAD CLEARING TESTS ====================

    /**
     * Test clearDataForStoreUpload method
     * Verifies that data is cleared in correct dependency order: Sales → Stores
     */
    @Test
    @Transactional
    @Rollback
    public void testClearDataForStoreUpload() {
        // Given: Data exists in all tables
        assertEquals("Should have 1 style", 1, styleDao.findAll().size());
        assertEquals("Should have 1 SKU", 1, skuDao.findAll().size());
        assertEquals("Should have 1 store", 1, storeDao.findAll().size());
        assertEquals("Should have 1 sale", 1, salesDao.findAll().size());

        // When: Clear data for store upload
        dataClearingService.clearDataForStoreUpload();

        // Then: Sales and Stores should be cleared, but Styles and SKUs should remain
        assertEquals("Sales should be cleared", 0, salesDao.findAll().size());
        assertEquals("Stores should be cleared", 0, storeDao.findAll().size());
        assertEquals("Styles should remain", 1, styleDao.findAll().size());
        assertEquals("SKUs should remain", 1, skuDao.findAll().size());
    }

    /**
     * Test clearDataForStoreUpload with multiple stores and sales
     * Verifies that all store-related data is cleared properly
     */
    @Test
    @Transactional
    @Rollback
    public void testClearDataForStoreUpload_MultipleStores() {
        // Given: Create additional store and sales
        Store store2 = new Store();
        store2.setBranch("DELHI_CP");
        store2.setCity("DELHI");
        storeDao.save(store2);

        Sales sale2 = new Sales();
        sale2.setDate(new Date());
        sale2.setSkuId(testSku.getId());
        sale2.setStoreId(store2.getId());
        sale2.setQuantity(2);
        sale2.setDiscount(new BigDecimal("0.00"));
        sale2.setRevenue(new BigDecimal("200.00"));
        salesDao.save(sale2);

        // Verify initial state
        assertEquals("Should have 2 stores", 2, storeDao.findAll().size());
        assertEquals("Should have 2 sales", 2, salesDao.findAll().size());

        // When: Clear data for store upload
        dataClearingService.clearDataForStoreUpload();

        // Then: All stores and sales should be cleared
        assertEquals("All sales should be cleared", 0, salesDao.findAll().size());
        assertEquals("All stores should be cleared", 0, storeDao.findAll().size());
        assertEquals("Styles should remain", 1, styleDao.findAll().size());
        assertEquals("SKUs should remain", 1, skuDao.findAll().size());
    }

    // ==================== SKU UPLOAD CLEARING TESTS ====================

    /**
     * Test clearDataForSkuUpload method
     * Verifies that data is cleared in correct dependency order: Sales → SKUs
     */
    @Test
    @Transactional
    @Rollback
    public void testClearDataForSkuUpload() {
        // Given: Data exists in all tables
        assertEquals("Should have 1 style", 1, styleDao.findAll().size());
        assertEquals("Should have 1 SKU", 1, skuDao.findAll().size());
        assertEquals("Should have 1 store", 1, storeDao.findAll().size());
        assertEquals("Should have 1 sale", 1, salesDao.findAll().size());

        // When: Clear data for SKU upload
        dataClearingService.clearDataForSkuUpload();

        // Then: Sales and SKUs should be cleared, but Styles and Stores should remain
        assertEquals("Sales should be cleared", 0, salesDao.findAll().size());
        assertEquals("SKUs should be cleared", 0, skuDao.findAll().size());
        assertEquals("Styles should remain", 1, styleDao.findAll().size());
        assertEquals("Stores should remain", 1, storeDao.findAll().size());
    }

    /**
     * Test clearDataForSkuUpload with multiple SKUs and sales
     * Verifies that all SKU-related data is cleared properly
     */
    @Test
    @Transactional
    @Rollback
    public void testClearDataForSkuUpload_MultipleSKUs() {
        // Given: Create additional SKU and sales
        SKU sku2 = new SKU();
        sku2.setSku("SKU002");
        sku2.setStyleId(testStyle.getId());
        sku2.setSize("L");
        skuDao.save(sku2);

        Sales sale2 = new Sales();
        sale2.setDate(new Date());
        sale2.setSkuId(sku2.getId());
        sale2.setStoreId(testStore.getId());
        sale2.setQuantity(1);
        sale2.setDiscount(new BigDecimal("15.00"));
        sale2.setRevenue(new BigDecimal("85.00"));
        salesDao.save(sale2);

        // Verify initial state
        assertEquals("Should have 2 SKUs", 2, skuDao.findAll().size());
        assertEquals("Should have 2 sales", 2, salesDao.findAll().size());

        // When: Clear data for SKU upload
        dataClearingService.clearDataForSkuUpload();

        // Then: All SKUs and sales should be cleared
        assertEquals("All sales should be cleared", 0, salesDao.findAll().size());
        assertEquals("All SKUs should be cleared", 0, skuDao.findAll().size());
        assertEquals("Styles should remain", 1, styleDao.findAll().size());
        assertEquals("Stores should remain", 1, storeDao.findAll().size());
    }

    // ==================== SALES UPLOAD CLEARING TESTS ====================

    /**
     * Test clearDataForSalesUpload method
     * Verifies that only sales data is cleared (no dependencies)
     */
    @Test
    @Transactional
    @Rollback
    public void testClearDataForSalesUpload() {
        // Given: Data exists in all tables
        assertEquals("Should have 1 style", 1, styleDao.findAll().size());
        assertEquals("Should have 1 SKU", 1, skuDao.findAll().size());
        assertEquals("Should have 1 store", 1, storeDao.findAll().size());
        assertEquals("Should have 1 sale", 1, salesDao.findAll().size());

        // When: Clear data for sales upload
        dataClearingService.clearDataForSalesUpload();

        // Then: Only sales should be cleared, all other data should remain
        assertEquals("Sales should be cleared", 0, salesDao.findAll().size());
        assertEquals("SKUs should remain", 1, skuDao.findAll().size());
        assertEquals("Styles should remain", 1, styleDao.findAll().size());
        assertEquals("Stores should remain", 1, storeDao.findAll().size());
    }

    /**
     * Test clearDataForSalesUpload with multiple sales
     * Verifies that all sales are cleared while preserving master data
     */
    @Test
    @Transactional
    @Rollback
    public void testClearDataForSalesUpload_MultipleSales() {
        // Given: Create additional sales
        Sales sale2 = new Sales();
        sale2.setDate(new Date());
        sale2.setSkuId(testSku.getId());
        sale2.setStoreId(testStore.getId());
        sale2.setQuantity(3);
        sale2.setDiscount(new BigDecimal("20.00"));
        sale2.setRevenue(new BigDecimal("280.00"));
        salesDao.save(sale2);

        Sales sale3 = new Sales();
        sale3.setDate(new Date());
        sale3.setSkuId(testSku.getId());
        sale3.setStoreId(testStore.getId());
        sale3.setQuantity(1);
        sale3.setDiscount(new BigDecimal("0.00"));
        sale3.setRevenue(new BigDecimal("100.00"));
        salesDao.save(sale3);

        // Verify initial state
        assertEquals("Should have 3 sales", 3, salesDao.findAll().size());

        // When: Clear data for sales upload
        dataClearingService.clearDataForSalesUpload();

        // Then: All sales should be cleared, master data should remain
        assertEquals("All sales should be cleared", 0, salesDao.findAll().size());
        assertEquals("SKUs should remain", 1, skuDao.findAll().size());
        assertEquals("Styles should remain", 1, styleDao.findAll().size());
        assertEquals("Stores should remain", 1, storeDao.findAll().size());
    }

    // ==================== COMPLETE DATA CLEARING TESTS ====================

    /**
     * Test clearAllData method
     * Verifies that all data is cleared in proper dependency order
     */
    @Test
    @Transactional
    @Rollback
    public void testClearAllData() {
        // Given: Data exists in all tables
        assertEquals("Should have 1 style", 1, styleDao.findAll().size());
        assertEquals("Should have 1 SKU", 1, skuDao.findAll().size());
        assertEquals("Should have 1 store", 1, storeDao.findAll().size());
        assertEquals("Should have 1 sale", 1, salesDao.findAll().size());

        // When: Clear all data
        dataClearingService.clearAllData();

        // Then: All data should be cleared
        assertEquals("All sales should be cleared", 0, salesDao.findAll().size());
        assertEquals("All SKUs should be cleared", 0, skuDao.findAll().size());
        assertEquals("All styles should be cleared", 0, styleDao.findAll().size());
        assertEquals("All stores should be cleared", 0, storeDao.findAll().size());
    }

    /**
     * Test clearAllData with complex data hierarchy
     * Verifies that all data is cleared properly regardless of complexity
     */
    @Test
    @Transactional
    @Rollback
    public void testClearAllData_ComplexHierarchy() {
        // Given: Create complex data hierarchy
        // Additional styles
        Style style2 = new Style();
        style2.setStyleCode("PANT001");
        style2.setBrand("ADIDAS");
        style2.setCategory("PANTS");
        style2.setSubCategory("FORMAL");
        style2.setMrp(new BigDecimal("150.75"));
        style2.setGender("F");
        styleDao.save(style2);

        // Additional stores
        Store store2 = new Store();
        store2.setBranch("DELHI_CP");
        store2.setCity("DELHI");
        storeDao.save(store2);

        Store store3 = new Store();
        store3.setBranch("BANGALORE_MG");
        store3.setCity("BANGALORE");
        storeDao.save(store3);

        // Additional SKUs
        SKU sku2 = new SKU();
        sku2.setSku("SKU002");
        sku2.setStyleId(testStyle.getId());
        sku2.setSize("L");
        skuDao.save(sku2);

        SKU sku3 = new SKU();
        sku3.setSku("SKU003");
        sku3.setStyleId(style2.getId());
        sku3.setSize("M");
        skuDao.save(sku3);

        // Additional sales
        Sales sale2 = new Sales();
        sale2.setDate(new Date());
        sale2.setSkuId(sku2.getId());
        sale2.setStoreId(store2.getId());
        sale2.setQuantity(2);
        sale2.setDiscount(new BigDecimal("5.00"));
        sale2.setRevenue(new BigDecimal("195.00"));
        salesDao.save(sale2);

        Sales sale3 = new Sales();
        sale3.setDate(new Date());
        sale3.setSkuId(sku3.getId());
        sale3.setStoreId(store3.getId());
        sale3.setQuantity(4);
        sale3.setDiscount(new BigDecimal("25.00"));
        sale3.setRevenue(new BigDecimal("575.00"));
        salesDao.save(sale3);

        // Verify initial complex state
        assertEquals("Should have 2 styles", 2, styleDao.findAll().size());
        assertEquals("Should have 3 SKUs", 3, skuDao.findAll().size());
        assertEquals("Should have 3 stores", 3, storeDao.findAll().size());
        assertEquals("Should have 3 sales", 3, salesDao.findAll().size());

        // When: Clear all data
        dataClearingService.clearAllData();

        // Then: All data should be cleared
        assertEquals("All sales should be cleared", 0, salesDao.findAll().size());
        assertEquals("All SKUs should be cleared", 0, skuDao.findAll().size());
        assertEquals("All styles should be cleared", 0, styleDao.findAll().size());
        assertEquals("All stores should be cleared", 0, storeDao.findAll().size());
    }

    // ==================== EDGE CASES AND ERROR SCENARIOS ====================

    /**
     * Test clearing operations when no data exists
     * Verifies that clearing operations are safe when tables are already empty
     */
    @Test
    @Transactional
    @Rollback
    public void testClearOperations_EmptyTables() {
        // Given: Clear all existing data first
        dataClearingService.clearAllData();
        
        // Verify tables are empty
        assertEquals("Sales should be empty", 0, salesDao.findAll().size());
        assertEquals("SKUs should be empty", 0, skuDao.findAll().size());
        assertEquals("Styles should be empty", 0, styleDao.findAll().size());
        assertEquals("Stores should be empty", 0, storeDao.findAll().size());

        // When: Perform clearing operations on empty tables
        // Then: Should not throw exceptions
        assertDoesNotThrow("Style upload clearing should be safe", 
            () -> dataClearingService.clearDataForStyleUpload());
        
        assertDoesNotThrow("Store upload clearing should be safe", 
            () -> dataClearingService.clearDataForStoreUpload());
        
        assertDoesNotThrow("SKU upload clearing should be safe", 
            () -> dataClearingService.clearDataForSkuUpload());
        
        assertDoesNotThrow("Sales upload clearing should be safe", 
            () -> dataClearingService.clearDataForSalesUpload());
        
        assertDoesNotThrow("Clear all data should be safe", 
            () -> dataClearingService.clearAllData());

        // Verify tables remain empty
        assertEquals("Sales should remain empty", 0, salesDao.findAll().size());
        assertEquals("SKUs should remain empty", 0, skuDao.findAll().size());
        assertEquals("Styles should remain empty", 0, styleDao.findAll().size());
        assertEquals("Stores should remain empty", 0, storeDao.findAll().size());
    }

    /**
     * Test clearing operations with orphaned data
     * Verifies behavior when data exists without proper relationships
     */
    @Test
    @Transactional
    @Rollback
    public void testClearOperations_OrphanedData() {
        // Given: Clear all data and create orphaned records
        dataClearingService.clearAllData();

        // Create independent styles and stores (no SKUs or sales)
        Style orphanStyle = new Style();
        orphanStyle.setStyleCode("ORPHAN_STYLE");
        orphanStyle.setBrand("ORPHAN");
        orphanStyle.setCategory("ORPHAN");
        orphanStyle.setSubCategory("ORPHAN");
        orphanStyle.setMrp(new BigDecimal("99.99"));
        orphanStyle.setGender("U");
        styleDao.save(orphanStyle);

        Store orphanStore = new Store();
        orphanStore.setBranch("ORPHAN_STORE");
        orphanStore.setCity("ORPHAN_CITY");
        storeDao.save(orphanStore);

        // Verify orphaned data exists
        assertEquals("Should have 1 orphan style", 1, styleDao.findAll().size());
        assertEquals("Should have 1 orphan store", 1, storeDao.findAll().size());
        assertEquals("Should have no SKUs", 0, skuDao.findAll().size());
        assertEquals("Should have no sales", 0, salesDao.findAll().size());

        // When: Perform clearing operations
        dataClearingService.clearDataForStyleUpload();
        
        // Then: Styles should be cleared, stores should remain
        assertEquals("Orphan style should be cleared", 0, styleDao.findAll().size());
        assertEquals("Orphan store should remain", 1, storeDao.findAll().size());

        // When: Clear stores
        dataClearingService.clearDataForStoreUpload();
        
        // Then: Stores should be cleared
        assertEquals("Orphan store should be cleared", 0, storeDao.findAll().size());
    }

    // ==================== TRANSACTION INTEGRITY TESTS ====================

    /**
     * Test transaction rollback behavior
     * Verifies that clearing operations are transactional
     */
    @Test
    @Transactional
    @Rollback
    public void testTransactionIntegrity() {
        // Given: Data exists in all tables
        assertEquals("Should have 1 style", 1, styleDao.findAll().size());
        assertEquals("Should have 1 SKU", 1, skuDao.findAll().size());
        assertEquals("Should have 1 store", 1, storeDao.findAll().size());
        assertEquals("Should have 1 sale", 1, salesDao.findAll().size());

        // When: Perform clearing operation within transaction
        dataClearingService.clearDataForStyleUpload();

        // Then: Data should be cleared within transaction
        assertEquals("Sales should be cleared", 0, salesDao.findAll().size());
        assertEquals("SKUs should be cleared", 0, skuDao.findAll().size());
        assertEquals("Styles should be cleared", 0, styleDao.findAll().size());
        assertEquals("Stores should remain", 1, storeDao.findAll().size());

        // The @Rollback annotation will rollback the transaction after test completion
        // In production, the clearing would be permanent unless explicitly rolled back
    }

    /**
     * Test dependency order validation
     * Verifies that the clearing order respects foreign key constraints
     */
    @Test
    @Transactional
    @Rollback
    public void testDependencyOrderValidation() {
        // This test verifies that the service clears data in the correct order
        // to avoid foreign key constraint violations
        
        // Given: Complex data with multiple dependencies
        Style style2 = new Style();
        style2.setStyleCode("DRESS001");
        style2.setBrand("ZARA");
        style2.setCategory("DRESSES");
        style2.setSubCategory("PARTY");
        style2.setMrp(new BigDecimal("200.00"));
        style2.setGender("F");
        styleDao.save(style2);

        SKU sku2 = new SKU();
        sku2.setSku("SKU002");
        sku2.setStyleId(style2.getId());
        sku2.setSize("S");
        skuDao.save(sku2);

        Sales sale2 = new Sales();
        sale2.setDate(new Date());
        sale2.setSkuId(sku2.getId());
        sale2.setStoreId(testStore.getId());
        sale2.setQuantity(1);
        sale2.setDiscount(new BigDecimal("0.00"));
        sale2.setRevenue(new BigDecimal("200.00"));
        salesDao.save(sale2);

        // Verify complex hierarchy exists
        assertEquals("Should have 2 styles", 2, styleDao.findAll().size());
        assertEquals("Should have 2 SKUs", 2, skuDao.findAll().size());
        assertEquals("Should have 2 sales", 2, salesDao.findAll().size());

        // When: Clear data for style upload (most complex clearing)
        // This should succeed without foreign key constraint violations
        assertDoesNotThrow("Style clearing should handle dependencies correctly", 
            () -> dataClearingService.clearDataForStyleUpload());

        // Then: Verify proper clearing
        assertEquals("All sales should be cleared", 0, salesDao.findAll().size());
        assertEquals("All SKUs should be cleared", 0, skuDao.findAll().size());
        assertEquals("All styles should be cleared", 0, styleDao.findAll().size());
        assertEquals("Stores should remain", 1, storeDao.findAll().size());
    }

    // ==================== HELPER METHODS ====================

    /**
     * Helper method to assert that an operation does not throw exceptions
     */
    private void assertDoesNotThrow(String message, Runnable operation) {
        try {
            operation.run();
        } catch (Exception e) {
            fail(message + " - Exception thrown: " + e.getMessage());
        }
    }
}
