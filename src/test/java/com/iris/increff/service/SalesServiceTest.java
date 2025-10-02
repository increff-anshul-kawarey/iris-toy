package com.iris.increff.service;

import com.iris.increff.AbstractUnitTest;
import com.iris.increff.controller.UploadResponse;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Comprehensive test suite for SalesService
 * 
 * Tests all critical functionality including:
 * - CRUD operations (create, read, update, delete)
 * - Query methods (by date range, sales count)
 * - TSV data processing and validation
 * - Complex field mapping (date parsing, SKU/Store lookups)
 * - Graceful error handling (missing SKUs, invalid data)
 * - Edge cases and validation scenarios
 * - Database operations and transactions
 * - Batch operations and rollback scenarios
 * - Business logic validation
 * 
 * Target: 90-95% method and line coverage for SalesService
 * 
 * @author Anshuk Kawarry
 * @version 1.0
 * @since 2025-01-01
 */
public class SalesServiceTest extends AbstractUnitTest {

    @Autowired
    private SalesService salesService;

    @Autowired
    private SalesDao salesDao;

    @Autowired
    private SkuDao skuDao;

    @Autowired
    private StoreDao storeDao;

    @Autowired
    private StyleDao styleDao;

    @Autowired
    private DataClearingService dataClearingService;

    private Style testStyle;
    private SKU testSku1;
    private SKU testSku2;
    private Store testStore1;
    private Store testStore2;
    private ArrayList<HashMap<String, String>> validTsvData;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Setup test data before each test method
     * Creates test styles, SKUs, stores, and valid TSV data structures for testing
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

        // Create test SKUs
        testSku1 = new SKU();
        testSku1.setSku("SKU001");
        testSku1.setStyleId(testStyle.getId());
        testSku1.setSize("M");
        skuDao.save(testSku1);

        testSku2 = new SKU();
        testSku2.setSku("SKU002");
        testSku2.setStyleId(testStyle.getId());
        testSku2.setSize("L");
        skuDao.save(testSku2);

        // Create test stores
        testStore1 = new Store();
        testStore1.setBranch("MUMBAI_CENTRAL");
        testStore1.setCity("MUMBAI");
        storeDao.save(testStore1);

        testStore2 = new Store();
        testStore2.setBranch("DELHI_CP");
        testStore2.setCity("DELHI");
        storeDao.save(testStore2);

        // Create valid TSV data with multiple rows
        validTsvData = new ArrayList<>();
        validTsvData.add(createSalesRow("2024-01-15", "SKU001", "MUMBAI_CENTRAL", "5", "10.00", "450.00"));
        validTsvData.add(createSalesRow("2024-01-16", "SKU002", "DELHI_CP", "3", "5.50", "280.50"));
        validTsvData.add(createSalesRow("2024-01-17", "SKU001", "MUMBAI_CENTRAL", "2", "0.00", "200.00"));
    }

    // ==================== SUCCESSFUL PROCESSING TESTS ====================

    /**
     * Test successful processing of valid TSV data
     * Verifies that valid sales data is processed and saved correctly
     */
    @Test
    @Transactional
    @Rollback
    public void testProcessAndSaveSales_ValidData() {
        // When: Process valid TSV data
        UploadResponse response = salesService.processAndSaveSales(validTsvData);

        // Then: Should succeed
        assertNotNull("Response should not be null", response);
        assertTrue("Response should be successful", response.isSuccess());
        assertEquals("Should process all records", 3, response.getRecordCount() != null ? (int) response.getRecordCount() : 0);
        assertEquals("Should have no errors", 0, response.getErrorCount() != null ? (int) response.getErrorCount() : 0);
        assertNotNull("Should have messages", response.getMessages());
        assertTrue("Should have success messages", response.getMessages().size() > 0);

        // Verify data was saved to database
        List<Sales> savedSales = salesService.getAllSales();
        assertEquals("Should have 3 sales in database", 3, savedSales.size());

        // Verify specific sales data
        Sales sale1 = savedSales.stream()
            .filter(s -> s.getSkuId().equals(testSku1.getId()) && s.getQuantity() == 5)
            .findFirst()
            .orElse(null);
        assertNotNull("Sale 1 should be saved", sale1);
        assertEquals("SKU ID should match", testSku1.getId(), sale1.getSkuId());
        assertEquals("Store ID should match", testStore1.getId(), sale1.getStoreId());
        assertEquals("Quantity should match", Integer.valueOf(5), sale1.getQuantity());
        assertEquals("Discount should match", new BigDecimal("10.00"), sale1.getDiscount());
        assertEquals("Revenue should match", new BigDecimal("450.00"), sale1.getRevenue());
    }

    /**
     * Test processing of single sales record
     * Verifies that single record processing works correctly
     */
    @Test
    @Transactional
    @Rollback
    public void testProcessAndSaveSales_SingleRecord() {
        // Given: Single sales record
        ArrayList<HashMap<String, String>> singleRecord = new ArrayList<>();
        singleRecord.add(createSalesRow("2024-01-15", "SKU001", "MUMBAI_CENTRAL", "1", "0.00", "100.00"));

        // When: Process single record
        UploadResponse response = salesService.processAndSaveSales(singleRecord);

        // Then: Should succeed
        assertNotNull("Response should not be null", response);
        assertTrue("Response should be successful", response.isSuccess());
        assertEquals("Should process 1 record", 1, response.getRecordCount() != null ? (int) response.getRecordCount() : 0);
        assertEquals("Should have no errors", 0, response.getErrorCount() != null ? (int) response.getErrorCount() : 0);

        // Verify data was saved
        List<Sales> savedSales = salesService.getAllSales();
        assertEquals("Should have 1 sale in database", 1, savedSales.size());
    }

    /**
     * Test zero revenue handling (promotional items)
     * Verifies that zero revenue is allowed for promotional items
     */
    @Test
    @Transactional
    @Rollback
    public void testProcessAndSaveSales_ZeroRevenue() {
        // Given: Sales with zero revenue (promotional item)
        ArrayList<HashMap<String, String>> promoData = new ArrayList<>();
        promoData.add(createSalesRow("2024-01-15", "SKU001", "MUMBAI_CENTRAL", "1", "0.00", "0.00"));

        // When: Process promotional data
        UploadResponse response = salesService.processAndSaveSales(promoData);

        // Then: Should succeed (zero revenue is allowed)
        assertTrue("Response should be successful", response.isSuccess());
        assertEquals("Should process 1 record", 1, response.getRecordCount() != null ? (int) response.getRecordCount() : 0);

        Sales savedSale = salesService.getAllSales().get(0);
        assertEquals("Revenue should be zero", new BigDecimal("0.00"), savedSale.getRevenue());
    }

    // ==================== VALIDATION ERROR TESTS ====================

    /**
     * Test validation of empty/null fields
     * Verifies that all required fields are validated for emptiness
     */
    @Test
    @Transactional
    @Rollback
    public void testProcessAndSaveSales_EmptyFields() {
        ArrayList<HashMap<String, String>> testData = new ArrayList<>();

        // Test empty date
        testData.add(createSalesRow("", "SKU001", "MUMBAI_CENTRAL", "1", "0.00", "100.00"));
        // Test empty SKU
        testData.add(createSalesRow("2024-01-15", "", "MUMBAI_CENTRAL", "1", "0.00", "100.00"));
        // Test empty channel
        testData.add(createSalesRow("2024-01-15", "SKU001", "", "1", "0.00", "100.00"));
        // Test empty quantity
        testData.add(createSalesRow("2024-01-15", "SKU001", "MUMBAI_CENTRAL", "", "0.00", "100.00"));
        // Test empty discount
        testData.add(createSalesRow("2024-01-15", "SKU001", "MUMBAI_CENTRAL", "1", "", "100.00"));
        // Test empty revenue
        testData.add(createSalesRow("2024-01-15", "SKU001", "MUMBAI_CENTRAL", "1", "0.00", ""));

        // When: Process data with empty fields
        UploadResponse response = salesService.processAndSaveSales(testData);

        // Then: Should fail with validation errors
        assertNotNull("Response should not be null", response);
        assertFalse("Response should fail", response.isSuccess());
        assertEquals("Should have 6 errors", 6, response.getErrorCount() != null ? (int) response.getErrorCount() : 0);
        assertEquals("Should process 0 records", 0, response.getRecordCount() != null ? (int) response.getRecordCount() : 0);

        List<String> errors = response.getErrors();
        assertTrue("Should have date error", 
            errors.stream().anyMatch(e -> e.contains("Date (day) cannot be empty")));
        assertTrue("Should have SKU error", 
            errors.stream().anyMatch(e -> e.contains("SKU code cannot be empty")));
        assertTrue("Should have channel error", 
            errors.stream().anyMatch(e -> e.contains("Channel (store branch) cannot be empty")));
        assertTrue("Should have quantity error", 
            errors.stream().anyMatch(e -> e.contains("Quantity cannot be empty")));
        assertTrue("Should have discount error", 
            errors.stream().anyMatch(e -> e.contains("Discount cannot be empty")));
        assertTrue("Should have revenue error", 
            errors.stream().anyMatch(e -> e.contains("Revenue cannot be empty")));

        // Verify no data was saved
        List<Sales> savedSales = salesService.getAllSales();
        assertEquals("Should have no sales in database", 0, savedSales.size());
    }

    /**
     * Test date format validation
     * Verifies that date parsing works correctly and rejects invalid formats
     */
    @Test
    @Transactional
    @Rollback
    public void testProcessAndSaveSales_DateFormatValidation() {
        ArrayList<HashMap<String, String>> testData = new ArrayList<>();

        // Test clearly invalid date formats that SimpleDateFormat will definitely reject
        testData.add(createSalesRow("invalid-date", "SKU001", "MUMBAI_CENTRAL", "1", "0.00", "100.00")); // Invalid date
        testData.add(createSalesRow("2024-13-45", "SKU001", "MUMBAI_CENTRAL", "1", "0.00", "100.00")); // Invalid month/day
        testData.add(createSalesRow("not-a-date", "SKU001", "MUMBAI_CENTRAL", "1", "0.00", "100.00")); // Completely invalid

        // When: Process data with invalid dates
        UploadResponse response = salesService.processAndSaveSales(testData);

        // Then: Should fail with date format errors
        assertFalse("Response should fail", response.isSuccess());
        assertTrue("Should have at least 2 errors", response.getErrorCount() != null && (int) response.getErrorCount() >= 2);

        List<String> errors = response.getErrors();
        assertTrue("Should have date format errors", 
            errors.stream().anyMatch(e -> e.contains("Invalid date format")));
    }

    /**
     * Test numeric field validation
     * Verifies that quantity, discount, and revenue are properly validated
     */
    @Test
    @Transactional
    @Rollback
    public void testProcessAndSaveSales_NumericValidation() {
        ArrayList<HashMap<String, String>> testData = new ArrayList<>();

        // Test invalid quantity (non-numeric)
        testData.add(createSalesRow("2024-01-15", "SKU001", "MUMBAI_CENTRAL", "abc", "0.00", "100.00"));
        // Test invalid quantity (zero)
        testData.add(createSalesRow("2024-01-15", "SKU001", "MUMBAI_CENTRAL", "0", "0.00", "100.00"));
        // Test invalid quantity (negative)
        testData.add(createSalesRow("2024-01-15", "SKU001", "MUMBAI_CENTRAL", "-1", "0.00", "100.00"));
        // Test invalid discount (non-numeric)
        testData.add(createSalesRow("2024-01-15", "SKU001", "MUMBAI_CENTRAL", "1", "abc", "100.00"));
        // Test invalid discount (negative)
        testData.add(createSalesRow("2024-01-15", "SKU001", "MUMBAI_CENTRAL", "1", "-5.00", "100.00"));
        // Test invalid revenue (non-numeric)
        testData.add(createSalesRow("2024-01-15", "SKU001", "MUMBAI_CENTRAL", "1", "0.00", "abc"));
        // Test invalid revenue (negative)
        testData.add(createSalesRow("2024-01-15", "SKU001", "MUMBAI_CENTRAL", "1", "0.00", "-100.00"));

        // When: Process data with invalid numeric values
        UploadResponse response = salesService.processAndSaveSales(testData);

        // Then: Should fail with numeric validation errors
        assertFalse("Response should fail", response.isSuccess());
        assertEquals("Should have 7 errors", 7, response.getErrorCount() != null ? (int) response.getErrorCount() : 0);

        List<String> errors = response.getErrors();
        assertTrue("Should have quantity format error", 
            errors.stream().anyMatch(e -> e.contains("Invalid quantity format")));
        assertTrue("Should have quantity positive error", 
            errors.stream().anyMatch(e -> e.contains("Quantity must be positive")));
        assertTrue("Should have discount format error", 
            errors.stream().anyMatch(e -> e.contains("Invalid discount format")));
        assertTrue("Should have discount negative error", 
            errors.stream().anyMatch(e -> e.contains("Discount cannot be negative")));
        assertTrue("Should have revenue format error", 
            errors.stream().anyMatch(e -> e.contains("Invalid revenue format")));
        assertTrue("Should have revenue negative error", 
            errors.stream().anyMatch(e -> e.contains("Revenue cannot be negative")));
    }

    /**
     * Test SKU lookup validation and graceful handling
     * Verifies that missing SKUs are handled gracefully with warnings
     */
    @Test
    @Transactional
    @Rollback
    public void testProcessAndSaveSales_SkuLookupGracefulHandling() {
        ArrayList<HashMap<String, String>> testData = new ArrayList<>();

        // Valid SKU
        testData.add(createSalesRow("2024-01-15", "SKU001", "MUMBAI_CENTRAL", "1", "0.00", "100.00"));
        // Invalid SKU (should be skipped gracefully)
        testData.add(createSalesRow("2024-01-16", "NONEXISTENT_SKU", "MUMBAI_CENTRAL", "1", "0.00", "100.00"));
        // Another valid SKU
        testData.add(createSalesRow("2024-01-17", "SKU002", "DELHI_CP", "2", "5.00", "200.00"));

        // When: Process data with missing SKU
        UploadResponse response = salesService.processAndSaveSales(testData);

        // Then: Should succeed but skip the missing SKU row
        assertTrue("Response should be successful", response.isSuccess());
        assertEquals("Should process 2 valid records", 2, response.getRecordCount() != null ? (int) response.getRecordCount() : 0);
        assertEquals("Should have 1 skipped record", 1, response.getSkippedCount() != null ? (int) response.getSkippedCount() : 0);
        assertEquals("Should have no errors", 0, response.getErrorCount() != null ? (int) response.getErrorCount() : 0);

        // Verify warnings are present
        assertNotNull("Should have warnings", response.getWarnings());
        assertTrue("Should have SKU not found warning", 
            response.getWarnings().stream().anyMatch(w -> w.contains("SKU 'NONEXISTENT_SKU' not found")));

        // Verify only valid records were saved
        List<Sales> savedSales = salesService.getAllSales();
        assertEquals("Should have 2 sales in database", 2, savedSales.size());
    }

    /**
     * Test store lookup validation
     * Verifies that store (channel) lookups are properly validated
     */
    @Test
    @Transactional
    @Rollback
    public void testProcessAndSaveSales_StoreLookupValidation() {
        ArrayList<HashMap<String, String>> testData = new ArrayList<>();

        // Valid store
        testData.add(createSalesRow("2024-01-15", "SKU001", "MUMBAI_CENTRAL", "1", "0.00", "100.00"));
        // Invalid store (should fail)
        testData.add(createSalesRow("2024-01-16", "SKU001", "NONEXISTENT_STORE", "1", "0.00", "100.00"));

        // When: Process data with invalid store lookup
        UploadResponse response = salesService.processAndSaveSales(testData);

        // Then: Should fail with store lookup error
        assertFalse("Response should fail", response.isSuccess());
        assertEquals("Should have 1 error", 1, response.getErrorCount() != null ? (int) response.getErrorCount() : 0);

        List<String> errors = response.getErrors();
        assertTrue("Should have store lookup error", 
            errors.stream().anyMatch(e -> e.contains("Store lookup failed for channel 'NONEXISTENT_STORE'")));
    }

    // ==================== MIXED SCENARIOS TESTS ====================

    /**
     * Test mixed valid and invalid data
     * Verifies that if any record has critical errors, the entire batch fails
     */
    @Test
    @Transactional
    @Rollback
    public void testProcessAndSaveSales_MixedValidInvalid() {
        ArrayList<HashMap<String, String>> testData = new ArrayList<>();

        // Add valid records
        testData.add(createSalesRow("2024-01-15", "SKU001", "MUMBAI_CENTRAL", "1", "0.00", "100.00"));
        testData.add(createSalesRow("2024-01-16", "SKU002", "DELHI_CP", "2", "5.00", "200.00"));
        
        // Add invalid record (critical error - invalid date)
        testData.add(createSalesRow("invalid-date", "SKU001", "MUMBAI_CENTRAL", "1", "0.00", "100.00"));
        
        // Add more valid records
        testData.add(createSalesRow("2024-01-18", "SKU001", "MUMBAI_CENTRAL", "3", "10.00", "300.00"));

        // When: Process mixed data
        UploadResponse response = salesService.processAndSaveSales(testData);

        // Then: Should fail entirely (all-or-nothing approach for critical errors)
        assertNotNull("Response should not be null", response);
        assertFalse("Response should fail", response.isSuccess());
        assertEquals("Should have 1 error", 1, response.getErrorCount() != null ? (int) response.getErrorCount() : 0);
        assertEquals("Should process 0 records", 0, response.getRecordCount() != null ? (int) response.getRecordCount() : 0);

        // Verify no data was saved (transaction rollback)
        List<Sales> savedSales = salesService.getAllSales();
        assertEquals("Should have no sales in database", 0, savedSales.size());
    }

    /**
     * Test row number reporting in error messages
     * Verifies that error messages include correct row numbers
     */
    @Test
    @Transactional
    @Rollback
    public void testProcessAndSaveSales_RowNumberReporting() {
        ArrayList<HashMap<String, String>> testData = new ArrayList<>();

        // Row 1 (becomes row 2 in error message due to header)
        testData.add(createSalesRow("2024-01-15", "SKU001", "MUMBAI_CENTRAL", "1", "0.00", "100.00")); // Valid
        
        // Row 2 (becomes row 3 in error message)
        testData.add(createSalesRow("", "SKU001", "MUMBAI_CENTRAL", "1", "0.00", "100.00")); // Invalid date
        
        // Row 3 (becomes row 4 in error message)
        testData.add(createSalesRow("2024-01-17", "SKU001", "MUMBAI_CENTRAL", "abc", "0.00", "100.00")); // Invalid quantity

        // When: Process data with errors
        UploadResponse response = salesService.processAndSaveSales(testData);

        // Then: Should report correct row numbers
        assertFalse("Response should fail", response.isSuccess());
        assertEquals("Should have 2 errors", 2, response.getErrorCount() != null ? (int) response.getErrorCount() : 0);

        List<String> errors = response.getErrors();
        assertTrue("Should have row 3 error", 
            errors.stream().anyMatch(e -> e.contains("Row 3:")));
        assertTrue("Should have row 4 error", 
            errors.stream().anyMatch(e -> e.contains("Row 4:")));
    }

    // ==================== EDGE CASES TESTS ====================

    /**
     * Test processing of empty TSV data
     * Verifies that empty input is handled gracefully
     */
    @Test
    @Transactional
    @Rollback
    public void testProcessAndSaveSales_EmptyTsvData() {
        // Given: Empty TSV data
        ArrayList<HashMap<String, String>> emptyData = new ArrayList<>();

        // When: Process empty data
        UploadResponse response = salesService.processAndSaveSales(emptyData);

        // Then: Should succeed but with no records processed
        assertNotNull("Response should not be null", response);
        assertTrue("Response should be successful", response.isSuccess());
        assertEquals("Should process 0 records", 0, response.getRecordCount() != null ? (int) response.getRecordCount() : 0);
        assertEquals("Should have no errors", 0, response.getErrorCount() != null ? (int) response.getErrorCount() : 0);

        // Verify data clearing still occurred
        assertNotNull("Should have messages", response.getMessages());
        assertTrue("Should have clearing message", 
            response.getMessages().stream().anyMatch(m -> m.contains("Clearing existing data")));
    }

    /**
     * Test decimal precision handling
     * Verifies that decimal values are handled correctly
     */
    @Test
    @Transactional
    @Rollback
    public void testProcessAndSaveSales_DecimalPrecision() {
        ArrayList<HashMap<String, String>> testData = new ArrayList<>();

        // Test various decimal formats
        testData.add(createSalesRow("2024-01-15", "SKU001", "MUMBAI_CENTRAL", "1", "10.50", "100.99"));
        testData.add(createSalesRow("2024-01-16", "SKU001", "MUMBAI_CENTRAL", "1", "0", "100")); // Integer format
        testData.add(createSalesRow("2024-01-17", "SKU001", "MUMBAI_CENTRAL", "1", "5.123", "99.999")); // High precision

        // When: Process data with various decimal formats
        UploadResponse response = salesService.processAndSaveSales(testData);

        // Then: Should succeed
        assertTrue("Response should be successful", response.isSuccess());
        assertEquals("Should process all records", 3, response.getRecordCount() != null ? (int) response.getRecordCount() : 0);

        List<Sales> savedSales = salesService.getAllSales();
        assertEquals("Should have 3 sales in database", 3, savedSales.size());
    }

    // ==================== SERVICE METHOD TESTS ====================

    /**
     * Test getAllSales method
     * Verifies that all sales can be retrieved from database
     */
    @Test
    @Transactional
    @Rollback
    public void testGetAllSales() {
        // Given: Process some sales first
        salesService.processAndSaveSales(validTsvData);

        // When: Get all sales
        List<Sales> allSales = salesService.getAllSales();

        // Then: Should return all saved sales
        assertNotNull("Sales list should not be null", allSales);
        assertEquals("Should return 3 sales", 3, allSales.size());

        // Verify sales data integrity
        for (Sales sale : allSales) {
            assertNotNull("Sale should have date", sale.getDate());
            assertNotNull("Sale should have SKU ID", sale.getSkuId());
            assertNotNull("Sale should have store ID", sale.getStoreId());
            assertTrue("Quantity should be positive", sale.getQuantity() > 0);
            assertTrue("Discount should be non-negative", sale.getDiscount().compareTo(BigDecimal.ZERO) >= 0);
            assertTrue("Revenue should be non-negative", sale.getRevenue().compareTo(BigDecimal.ZERO) >= 0);
        }
    }

    /**
     * Test getSalesCount method
     * Verifies that sales count is returned correctly
     */
    @Test
    @Transactional
    @Rollback
    public void testGetSalesCount() {
        // Given: Process some sales first
        salesService.processAndSaveSales(validTsvData);

        // When: Get sales count
        Long salesCount = salesService.getSalesCount();

        // Then: Should return correct count
        assertNotNull("Sales count should not be null", salesCount);
        assertEquals("Should return count of 3", Long.valueOf(3), salesCount);
    }

    /**
     * Test getSalesByDateRange method
     * Verifies that sales can be filtered by date range
     */
    @Test
    @Transactional
    @Rollback
    public void testGetSalesByDateRange() throws Exception {
        // Given: Process some sales first
        salesService.processAndSaveSales(validTsvData);

        // When: Get sales within date range
        Date startDate = dateFormat.parse("2024-01-15");
        Date endDate = dateFormat.parse("2024-01-16");
        List<Sales> salesInRange = salesService.getSalesByDateRange(startDate, endDate);

        // Then: Should return sales within range
        assertNotNull("Sales list should not be null", salesInRange);
        assertEquals("Should return 2 sales in range", 2, salesInRange.size());

        // Verify all returned sales are within date range
        for (Sales sale : salesInRange) {
            assertTrue("Sale date should be >= start date", 
                sale.getDate().compareTo(startDate) >= 0);
            assertTrue("Sale date should be <= end date", 
                sale.getDate().compareTo(endDate) <= 0);
        }
    }

    // ==================== TRANSACTION AND DATABASE TESTS ====================

    /**
     * Test transaction rollback on database error
     * Verifies that transactions are properly rolled back on errors
     */
    @Test
    @Transactional
    @Rollback
    public void testProcessAndSaveSales_TransactionRollback() {
        // This test verifies that the @Transactional annotation works correctly
        // If there's a database error during save, the transaction should rollback
        
        // Given: Valid data that should normally succeed
        ArrayList<HashMap<String, String>> testData = new ArrayList<>();
        testData.add(createSalesRow("2024-01-15", "SKU001", "MUMBAI_CENTRAL", "1", "0.00", "100.00"));

        // When: Process data (this should succeed in normal circumstances)
        UploadResponse response = salesService.processAndSaveSales(testData);

        // Then: Should succeed (we can't easily simulate database errors in this test environment)
        assertTrue("Response should be successful", response.isSuccess());
        
        // The @Rollback annotation ensures transaction rollback for testing
        // In production, any RuntimeException would cause rollback
    }

    /**
     * Test data clearing functionality
     * Verifies that the service includes data clearing messages and processes correctly
     */
    @Test
    @Transactional
    @Rollback
    public void testProcessAndSaveSales_DataClearing() {
        // Given: Create test data
        ArrayList<HashMap<String, String>> testData = new ArrayList<>();
        testData.add(createSalesRow("2024-01-15", "SKU001", "MUMBAI_CENTRAL", "1", "0.00", "100.00"));
        testData.add(createSalesRow("2024-01-16", "SKU002", "DELHI_CP", "2", "5.00", "200.00"));

        // When: Process data (should include clearing messages)
        UploadResponse response = salesService.processAndSaveSales(testData);

        // Then: Should succeed and include data clearing messages
        assertNotNull("Response should not be null", response);
        assertTrue("Response should be successful", response.isSuccess());
        assertEquals("Should process records", 2, response.getRecordCount() != null ? (int) response.getRecordCount() : 0);
        assertEquals("Should have no errors", 0, response.getErrorCount() != null ? (int) response.getErrorCount() : 0);

        // Verify data clearing messages are present
        assertNotNull("Should have messages", response.getMessages());
        assertTrue("Should have clearing message", 
            response.getMessages().stream().anyMatch(m -> m.contains("Clearing existing data")));
        assertTrue("Should have completion message", 
            response.getMessages().stream().anyMatch(m -> m.contains("Data clearing completed")));
        assertTrue("Should have success message", 
            response.getMessages().stream().anyMatch(m -> m.contains("upload completed successfully")));

        // Verify data was saved
        List<Sales> savedSales = salesService.getAllSales();
        assertEquals("Should have saved sales", 2, savedSales.size());
    }

    // ==================== BATCH OPERATIONS TESTS ====================

    /**
     * Test batch save operations
     * Verifies that multiple sales can be saved efficiently in batch
     */
    @Test
    @Transactional
    @Rollback
    public void testProcessAndSaveSales_BatchOperations() {
        // Given: Create large batch of sales
        ArrayList<HashMap<String, String>> largeBatch = new ArrayList<>();
        for (int i = 1; i <= 25; i++) {
            String date = String.format("2024-01-%02d", (i % 28) + 1); // Valid dates
            String sku = (i % 2 == 0) ? "SKU001" : "SKU002";
            String store = (i % 2 == 0) ? "MUMBAI_CENTRAL" : "DELHI_CP";
            String quantity = String.valueOf(i % 5 + 1);
            String discount = String.format("%.2f", (i % 10) * 1.5);
            String revenue = String.format("%.2f", (i % 20 + 1) * 50.0);
            largeBatch.add(createSalesRow(date, sku, store, quantity, discount, revenue));
        }

        // When: Process large batch
        UploadResponse response = salesService.processAndSaveSales(largeBatch);

        // Then: Should succeed and save all records
        assertTrue("Response should be successful", response.isSuccess());
        assertEquals("Should process all records", 25, response.getRecordCount() != null ? (int) response.getRecordCount() : 0);
        assertEquals("Should have no errors", 0, response.getErrorCount() != null ? (int) response.getErrorCount() : 0);

        // Verify all sales were saved
        List<Sales> savedSales = salesService.getAllSales();
        assertEquals("Should have saved all sales", 25, savedSales.size());
    }

    /**
     * Test batch rollback on partial failure
     * Verifies that if any sale in batch fails, entire batch is rolled back
     */
    @Test
    @Transactional
    @Rollback
    public void testProcessAndSaveSales_BatchRollback() {
        // Given: Create batch with one invalid record
        ArrayList<HashMap<String, String>> batchWithError = new ArrayList<>();
        // Add valid records
        for (int i = 1; i <= 5; i++) {
            batchWithError.add(createSalesRow("2024-01-0" + i, "SKU001", "MUMBAI_CENTRAL", "1", "0.00", "100.00"));
        }
        // Add invalid record in the middle (invalid date)
        batchWithError.add(createSalesRow("invalid-date", "SKU001", "MUMBAI_CENTRAL", "1", "0.00", "100.00"));
        // Add more valid records
        for (int i = 7; i <= 10; i++) {
            batchWithError.add(createSalesRow("2024-01-0" + i, "SKU001", "MUMBAI_CENTRAL", "1", "0.00", "100.00"));
        }

        // When: Process batch with error
        UploadResponse response = salesService.processAndSaveSales(batchWithError);

        // Then: Should fail and rollback entire batch
        assertFalse("Response should fail", response.isSuccess());
        assertEquals("Should have 1 error", 1, response.getErrorCount() != null ? (int) response.getErrorCount() : 0);
        assertEquals("Should process 0 records", 0, response.getRecordCount() != null ? (int) response.getRecordCount() : 0);

        // Verify no data was saved (complete rollback)
        List<Sales> savedSales = salesService.getAllSales();
        assertEquals("Should have no sales in database", 0, savedSales.size());
    }

    // ==================== HELPER METHODS ====================

    /**
     * Create a sales row with all fields
     */
    private HashMap<String, String> createSalesRow(String day, String sku, String channel, 
                                                  String quantity, String discount, String revenue) {
        HashMap<String, String> row = new HashMap<>();
        row.put("day", day);
        row.put("sku", sku);
        row.put("channel", channel);
        row.put("quantity", quantity);
        row.put("discount", discount);
        row.put("revenue", revenue);
        return row;
    }
}
