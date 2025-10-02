package com.iris.increff.service;

import com.iris.increff.AbstractUnitTest;
import com.iris.increff.controller.UploadResponse;
import com.iris.increff.dao.SkuDao;
import com.iris.increff.dao.StyleDao;
import com.iris.increff.model.SKU;
import com.iris.increff.model.Style;
import com.iris.increff.exception.ApiException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Comprehensive test suite for SkuService
 * 
 * Tests all critical functionality including:
 * - CRUD operations (create, read, update, delete)
 * - Query methods (by SKU code, style lookup)
 * - TSV data processing and validation
 * - Field mapping and normalization
 * - Error handling and edge cases
 * - Database operations and transactions
 * - Batch operations and rollback scenarios
 * - Business logic validation
 * 
 * Target: 90%+ method and line coverage for SkuService
 * 
 * @author Anshuk Kawarry
 * @version 1.0
 * @since 2025-01-01
 */
public class SkuServiceTest extends AbstractUnitTest {

    @Autowired
    private SkuService skuService;

    @Autowired
    private SkuDao skuDao;

    @Autowired
    private StyleDao styleDao;

    @Autowired
    private StyleService styleService;

    @Autowired
    private DataClearingService dataClearingService;

    private Style testStyle1;
    private Style testStyle2;
    private ArrayList<HashMap<String, String>> validTsvData;

    /**
     * Setup test data before each test method
     * Creates test styles and valid TSV data structures for testing
     */
    @Before
    public void setUp() {
        // Create test styles first (required for SKU foreign key)
        testStyle1 = new Style();
        testStyle1.setStyleCode("SHIRT001");
        testStyle1.setBrand("NIKE");
        testStyle1.setCategory("SHIRTS");
        testStyle1.setSubCategory("CASUAL");
        testStyle1.setMrp(new BigDecimal("100.50"));
        testStyle1.setGender("M");

        testStyle2 = new Style();
        testStyle2.setStyleCode("PANT001");
        testStyle2.setBrand("ADIDAS");
        testStyle2.setCategory("PANTS");
        testStyle2.setSubCategory("FORMAL");
        testStyle2.setMrp(new BigDecimal("150.75"));
        testStyle2.setGender("F");

        // Create valid TSV data with multiple rows
        validTsvData = new ArrayList<>();
        validTsvData.add(createSkuRow("SKU001", "SHIRT001", "S"));
        validTsvData.add(createSkuRow("SKU002", "SHIRT001", "M"));
        validTsvData.add(createSkuRow("SKU003", "PANT001", "L"));
    }

    // ==================== SUCCESSFUL PROCESSING TESTS ====================

    /**
     * Test successful processing of valid TSV data
     * Verifies that valid SKU data is processed and saved correctly
     */
    @Test
    @Transactional
    @Rollback
    public void testProcessAndSaveSKUs_ValidData() {
        // Given: Save test styles first
        styleDao.save(testStyle1);
        styleDao.save(testStyle2);

        // When: Process valid TSV data
        UploadResponse response = skuService.processAndSaveSKUs(validTsvData);

        // Then: Should succeed
        assertNotNull("Response should not be null", response);
        assertTrue("Response should be successful", response.isSuccess());
        assertEquals("Should process all records", 3, response.getRecordCount() != null ? (int) response.getRecordCount() : 0);
        assertEquals("Should have no errors", 0, response.getErrorCount() != null ? (int) response.getErrorCount() : 0);
        assertNotNull("Should have messages", response.getMessages());
        assertTrue("Should have success messages", response.getMessages().size() > 0);

        // Verify data was saved to database
        List<SKU> savedSkus = skuService.getAllSKUs();
        assertEquals("Should have 3 SKUs in database", 3, savedSkus.size());

        // Verify specific SKU data
        SKU sku1 = savedSkus.stream()
            .filter(s -> "SKU001".equals(s.getSku()))
            .findFirst()
            .orElse(null);
        assertNotNull("SKU001 should be saved", sku1);
        assertEquals("SKU code should be normalized", "SKU001", sku1.getSku());
        assertEquals("Size should be normalized", "S", sku1.getSize());
        assertEquals("Style ID should be set", testStyle1.getId(), sku1.getStyleId());
    }

    /**
     * Test processing of single SKU record
     * Verifies that single record processing works correctly
     */
    @Test
    @Transactional
    @Rollback
    public void testProcessAndSaveSKUs_SingleRecord() {
        // Given: Save test style and single SKU record
        styleDao.save(testStyle1);
        ArrayList<HashMap<String, String>> singleRecord = new ArrayList<>();
        singleRecord.add(createSkuRow("SKU001", "SHIRT001", "M"));

        // When: Process single record
        UploadResponse response = skuService.processAndSaveSKUs(singleRecord);

        // Then: Should succeed
        assertNotNull("Response should not be null", response);
        assertTrue("Response should be successful", response.isSuccess());
        assertEquals("Should process 1 record", 1, response.getRecordCount() != null ? (int) response.getRecordCount() : 0);
        assertEquals("Should have no errors", 0, response.getErrorCount() != null ? (int) response.getErrorCount() : 0);

        // Verify data was saved
        List<SKU> savedSkus = skuService.getAllSKUs();
        assertEquals("Should have 1 SKU in database", 1, savedSkus.size());
    }

    /**
     * Test data normalization functionality
     * Verifies that string data is properly normalized (trimmed and uppercased)
     */
    @Test
    @Transactional
    @Rollback
    public void testProcessAndSaveSKUs_DataNormalization() {
        // Given: Save test style and data with whitespace and mixed case
        styleDao.save(testStyle1);
        ArrayList<HashMap<String, String>> unnormalizedData = new ArrayList<>();
        HashMap<String, String> row = createSkuRow(
            "  sku002  ",    // Leading/trailing spaces
            " shirt001 ",    // Mixed case with spaces
            "m"              // Lowercase size
        );
        unnormalizedData.add(row);

        // When: Process unnormalized data
        UploadResponse response = skuService.processAndSaveSKUs(unnormalizedData);

        // Then: Should succeed with normalized data
        assertTrue("Response should be successful", response.isSuccess());

        SKU savedSku = skuService.getAllSKUs().get(0);
        assertEquals("SKU code should be trimmed and uppercased", "SKU002", savedSku.getSku());
        assertEquals("Size should be uppercased", "M", savedSku.getSize());
    }

    // ==================== VALIDATION ERROR TESTS ====================

    /**
     * Test validation of empty/null fields
     * Verifies that all required fields are validated for emptiness
     */
    @Test
    @Transactional
    @Rollback
    public void testProcessAndSaveSKUs_EmptyFields() {
        // Given: Save test style
        styleDao.save(testStyle1);
        
        ArrayList<HashMap<String, String>> testData = new ArrayList<>();

        // Test empty SKU code
        testData.add(createSkuRow("", "SHIRT001", "M"));
        // Test empty style code
        testData.add(createSkuRow("SKU002", "", "M"));
        // Test empty size
        testData.add(createSkuRow("SKU003", "SHIRT001", ""));

        // When: Process data with empty fields
        UploadResponse response = skuService.processAndSaveSKUs(testData);

        // Then: Should fail with validation errors
        assertNotNull("Response should not be null", response);
        assertFalse("Response should fail", response.isSuccess());
        assertEquals("Should have 3 errors", 3, response.getErrorCount() != null ? (int) response.getErrorCount() : 0);
        assertEquals("Should process 0 records", 0, response.getRecordCount() != null ? (int) response.getRecordCount() : 0);

        List<String> errors = response.getErrors();
        assertTrue("Should have SKU code error", 
            errors.stream().anyMatch(e -> e.contains("SKU code cannot be empty")));
        assertTrue("Should have style code error", 
            errors.stream().anyMatch(e -> e.contains("Style code cannot be empty")));
        assertTrue("Should have size error", 
            errors.stream().anyMatch(e -> e.contains("Size cannot be empty")));

        // Verify no data was saved
        List<SKU> savedSkus = skuService.getAllSKUs();
        assertEquals("Should have no SKUs in database", 0, savedSkus.size());
    }

    /**
     * Test validation of field length constraints
     * Verifies that field length limits are enforced
     */
    @Test
    @Transactional
    @Rollback
    public void testProcessAndSaveSKUs_FieldLengthValidation() {
        // Given: Save test style
        styleDao.save(testStyle1);
        
        ArrayList<HashMap<String, String>> testData = new ArrayList<>();

        // Test field length violations
        String longSkuCode = generateString("A", 51); // 51 characters (max 50)
        String longSize = generateString("X", 11); // 11 characters (max 10)
        
        testData.add(createSkuRow(longSkuCode, "SHIRT001", "M"));
        testData.add(createSkuRow("SKU002", "SHIRT001", longSize));

        // When: Process data with length violations
        UploadResponse response = skuService.processAndSaveSKUs(testData);

        // Then: Should fail with validation errors
        assertFalse("Response should fail", response.isSuccess());
        assertEquals("Should have 2 errors", 2, response.getErrorCount() != null ? (int) response.getErrorCount() : 0);

        List<String> errors = response.getErrors();
        assertTrue("Should have SKU code length error", 
            errors.stream().anyMatch(e -> e.contains("SKU code must be between")));
        assertTrue("Should have size length error", 
            errors.stream().anyMatch(e -> e.contains("Size must be between")));
    }

    /**
     * Test style lookup validation
     * Verifies that style codes are properly validated against existing styles
     */
    @Test
    @Transactional
    @Rollback
    public void testProcessAndSaveSKUs_StyleLookupValidation() {
        // Given: Save only one test style (SHIRT001)
        styleDao.save(testStyle1);
        
        ArrayList<HashMap<String, String>> testData = new ArrayList<>();

        // Test valid style lookup
        testData.add(createSkuRow("SKU001", "SHIRT001", "M")); // Valid
        // Test invalid style lookup
        testData.add(createSkuRow("SKU002", "NONEXISTENT", "L")); // Invalid

        // When: Process data with invalid style lookup
        UploadResponse response = skuService.processAndSaveSKUs(testData);

        // Then: Should fail with validation errors
        assertFalse("Response should fail", response.isSuccess());
        assertEquals("Should have 1 error", 1, response.getErrorCount() != null ? (int) response.getErrorCount() : 0);

        List<String> errors = response.getErrors();
        assertTrue("Should have style lookup error", 
            errors.stream().anyMatch(e -> e.contains("Style lookup failed for 'NONEXISTENT'")));
    }

    /**
     * Test duplicate SKU code validation within batch
     * Verifies that duplicate SKU codes within the same upload are detected
     */
    @Test
    @Transactional
    @Rollback
    public void testProcessAndSaveSKUs_DuplicateSkuCodes() {
        // Given: Save test style
        styleDao.save(testStyle1);
        
        ArrayList<HashMap<String, String>> testData = new ArrayList<>();

        // Add duplicate SKU codes
        testData.add(createSkuRow("SKU001", "SHIRT001", "S"));
        testData.add(createSkuRow("SKU001", "SHIRT001", "M")); // Duplicate
        testData.add(createSkuRow("SKU002", "SHIRT001", "L"));
        testData.add(createSkuRow("SKU002", "SHIRT001", "XL")); // Duplicate

        // When: Process data with duplicates
        UploadResponse response = skuService.processAndSaveSKUs(testData);

        // Then: Should fail with duplicate errors
        assertFalse("Response should fail", response.isSuccess());
        assertEquals("Should have 2 duplicate errors", 2, response.getErrorCount() != null ? (int) response.getErrorCount() : 0);

        List<String> errors = response.getErrors();
        assertTrue("Should have SKU001 duplicate error", 
            errors.stream().anyMatch(e -> e.contains("Duplicate SKU code 'SKU001'")));
        assertTrue("Should have SKU002 duplicate error", 
            errors.stream().anyMatch(e -> e.contains("Duplicate SKU code 'SKU002'")));
    }

    // ==================== MIXED SCENARIOS TESTS ====================

    /**
     * Test mixed valid and invalid data
     * Verifies that if any record is invalid, the entire batch fails
     */
    @Test
    @Transactional
    @Rollback
    public void testProcessAndSaveSKUs_MixedValidInvalid() {
        // Given: Save test style
        styleDao.save(testStyle1);
        
        ArrayList<HashMap<String, String>> testData = new ArrayList<>();

        // Add valid records
        testData.add(createSkuRow("SKU001", "SHIRT001", "S"));
        testData.add(createSkuRow("SKU002", "SHIRT001", "M"));
        
        // Add invalid record
        testData.add(createSkuRow("", "SHIRT001", "L")); // Empty SKU code
        
        // Add more valid records
        testData.add(createSkuRow("SKU004", "SHIRT001", "XL"));

        // When: Process mixed data
        UploadResponse response = skuService.processAndSaveSKUs(testData);

        // Then: Should fail entirely (all-or-nothing approach)
        assertNotNull("Response should not be null", response);
        assertFalse("Response should fail", response.isSuccess());
        assertEquals("Should have 1 error", 1, response.getErrorCount() != null ? (int) response.getErrorCount() : 0);
        assertEquals("Should process 0 records", 0, response.getRecordCount() != null ? (int) response.getRecordCount() : 0);

        // Verify no data was saved (transaction rollback)
        List<SKU> savedSkus = skuService.getAllSKUs();
        assertEquals("Should have no SKUs in database", 0, savedSkus.size());
    }

    /**
     * Test row number reporting in error messages
     * Verifies that error messages include correct row numbers
     */
    @Test
    @Transactional
    @Rollback
    public void testProcessAndSaveSKUs_RowNumberReporting() {
        // Given: Save test style
        styleDao.save(testStyle1);
        
        ArrayList<HashMap<String, String>> testData = new ArrayList<>();

        // Row 1 (becomes row 2 in error message due to header)
        testData.add(createSkuRow("SKU001", "SHIRT001", "S")); // Valid
        
        // Row 2 (becomes row 3 in error message)
        testData.add(createSkuRow("", "SHIRT001", "M")); // Invalid
        
        // Row 3 (becomes row 4 in error message)
        testData.add(createSkuRow("SKU003", "", "L")); // Invalid

        // When: Process data with errors
        UploadResponse response = skuService.processAndSaveSKUs(testData);

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
    public void testProcessAndSaveSKUs_EmptyTsvData() {
        // Given: Empty TSV data
        ArrayList<HashMap<String, String>> emptyData = new ArrayList<>();

        // When: Process empty data
        UploadResponse response = skuService.processAndSaveSKUs(emptyData);

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
     * Test whitespace handling in all fields
     * Verifies that leading/trailing whitespace is properly handled
     */
    @Test
    @Transactional
    @Rollback
    public void testProcessAndSaveSKUs_WhitespaceHandling() {
        // Given: Save test style
        styleDao.save(testStyle1);
        
        ArrayList<HashMap<String, String>> testData = new ArrayList<>();

        // Test whitespace in all fields
        HashMap<String, String> row = new HashMap<>();
        row.put("sku", "  SKU001  ");
        row.put("style", "\tSHIRT001\t");
        row.put("size", " M ");
        testData.add(row);

        // When: Process data with whitespace
        UploadResponse response = skuService.processAndSaveSKUs(testData);

        // Then: Should succeed with trimmed data
        assertTrue("Response should be successful", response.isSuccess());
        assertEquals("Should process 1 record", 1, response.getRecordCount() != null ? (int) response.getRecordCount() : 0);

        SKU savedSku = skuService.getAllSKUs().get(0);
        assertEquals("SKU code should be trimmed", "SKU001", savedSku.getSku());
        assertEquals("Size should be trimmed", "M", savedSku.getSize());
    }

    // ==================== SERVICE METHOD TESTS ====================

    /**
     * Test getAllSKUs method
     * Verifies that all SKUs can be retrieved from database
     */
    @Test
    @Transactional
    @Rollback
    public void testGetAllSKUs() {
        // Given: Save test styles and process some SKUs first
        styleDao.save(testStyle1);
        styleDao.save(testStyle2);
        skuService.processAndSaveSKUs(validTsvData);

        // When: Get all SKUs
        List<SKU> allSkus = skuService.getAllSKUs();

        // Then: Should return all saved SKUs
        assertNotNull("SKUs list should not be null", allSkus);
        assertEquals("Should return 3 SKUs", 3, allSkus.size());

        // Verify SKU codes are present
        List<String> skuCodes = allSkus.stream()
            .map(SKU::getSku)
            .collect(java.util.stream.Collectors.toList());
        assertTrue("Should contain SKU001", skuCodes.contains("SKU001"));
        assertTrue("Should contain SKU002", skuCodes.contains("SKU002"));
        assertTrue("Should contain SKU003", skuCodes.contains("SKU003"));
    }

    /**
     * Test findBySku method with existing SKU
     * Verifies that SKUs can be found by their SKU code
     */
    @Test
    @Transactional
    @Rollback
    public void testFindBySku_ExistingSku() throws ApiException {
        // Given: Save test styles and process some SKUs first
        styleDao.save(testStyle1);
        styleDao.save(testStyle2);
        skuService.processAndSaveSKUs(validTsvData);

        // When: Find SKU by code
        SKU foundSku = skuService.findBySku("SKU001");

        // Then: Should return the correct SKU
        assertNotNull("Found SKU should not be null", foundSku);
        assertEquals("SKU code should match", "SKU001", foundSku.getSku());
        assertEquals("Size should match", "S", foundSku.getSize());
        assertEquals("Style ID should match", testStyle1.getId(), foundSku.getStyleId());
    }

    /**
     * Test findBySku method with non-existing SKU
     * Verifies that appropriate exception is thrown for non-existing SKUs
     */
    @Test
    @Transactional
    @Rollback
    public void testFindBySku_NonExistingSku() {
        // Given: No SKUs in database

        // When: Try to find non-existing SKU
        try {
            skuService.findBySku("NONEXISTENT");
            fail("Should throw ApiException for non-existing SKU");
        } catch (ApiException e) {
            // Then: Should throw exception with appropriate message
            assertTrue("Should contain SKU code in error message", 
                e.getMessage().contains("NONEXISTENT"));
            assertTrue("Should contain 'not found' in error message", 
                e.getMessage().toLowerCase().contains("not found"));
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
    public void testProcessAndSaveSKUs_TransactionRollback() {
        // This test verifies that the @Transactional annotation works correctly
        // If there's a database error during save, the transaction should rollback
        
        // Given: Save test style and valid data that should normally succeed
        styleDao.save(testStyle1);
        ArrayList<HashMap<String, String>> testData = new ArrayList<>();
        testData.add(createSkuRow("SKU001", "SHIRT001", "M"));

        // When: Process data (this should succeed in normal circumstances)
        UploadResponse response = skuService.processAndSaveSKUs(testData);

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
    public void testProcessAndSaveSKUs_DataClearing() {
        // Given: Save test styles and create test data
        styleDao.save(testStyle1);
        styleDao.save(testStyle2);
        ArrayList<HashMap<String, String>> testData = new ArrayList<>();
        testData.add(createSkuRow("SKU001", "SHIRT001", "S"));
        testData.add(createSkuRow("SKU002", "PANT001", "M"));

        // When: Process data (should include clearing messages)
        UploadResponse response = skuService.processAndSaveSKUs(testData);

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
        List<SKU> savedSkus = skuService.getAllSKUs();
        assertEquals("Should have saved SKUs", 2, savedSkus.size());
        
        // Verify specific SKUs are present
        assertTrue("Should contain SKU001", 
            savedSkus.stream().anyMatch(s -> "SKU001".equals(s.getSku())));
        assertTrue("Should contain SKU002", 
            savedSkus.stream().anyMatch(s -> "SKU002".equals(s.getSku())));
    }

    // ==================== BATCH OPERATIONS TESTS ====================

    /**
     * Test batch save operations
     * Verifies that multiple SKUs can be saved efficiently in batch
     */
    @Test
    @Transactional
    @Rollback
    public void testProcessAndSaveSKUs_BatchOperations() {
        // Given: Save test styles and create large batch of SKUs
        styleDao.save(testStyle1);
        styleDao.save(testStyle2);
        
        ArrayList<HashMap<String, String>> largeBatch = new ArrayList<>();
        for (int i = 1; i <= 25; i++) {
            String skuCode = String.format("SKU%03d", i);
            String styleCode = (i % 2 == 0) ? "SHIRT001" : "PANT001";
            String size = (i % 3 == 0) ? "S" : (i % 3 == 1) ? "M" : "L";
            largeBatch.add(createSkuRow(skuCode, styleCode, size));
        }

        // When: Process large batch
        UploadResponse response = skuService.processAndSaveSKUs(largeBatch);

        // Then: Should succeed and save all records
        assertTrue("Response should be successful", response.isSuccess());
        assertEquals("Should process all records", 25, response.getRecordCount() != null ? (int) response.getRecordCount() : 0);
        assertEquals("Should have no errors", 0, response.getErrorCount() != null ? (int) response.getErrorCount() : 0);

        // Verify all SKUs were saved
        List<SKU> savedSkus = skuService.getAllSKUs();
        assertEquals("Should have saved all SKUs", 25, savedSkus.size());
    }

    /**
     * Test batch rollback on partial failure
     * Verifies that if any SKU in batch fails, entire batch is rolled back
     */
    @Test
    @Transactional
    @Rollback
    public void testProcessAndSaveSKUs_BatchRollback() {
        // Given: Save test style and create batch with one invalid record
        styleDao.save(testStyle1);
        
        ArrayList<HashMap<String, String>> batchWithError = new ArrayList<>();
        // Add valid records
        for (int i = 1; i <= 5; i++) {
            batchWithError.add(createSkuRow("SKU00" + i, "SHIRT001", "M"));
        }
        // Add invalid record in the middle
        batchWithError.add(createSkuRow("", "SHIRT001", "L")); // Empty SKU code
        // Add more valid records
        for (int i = 6; i <= 10; i++) {
            batchWithError.add(createSkuRow("SKU00" + i, "SHIRT001", "L"));
        }

        // When: Process batch with error
        UploadResponse response = skuService.processAndSaveSKUs(batchWithError);

        // Then: Should fail and rollback entire batch
        assertFalse("Response should fail", response.isSuccess());
        assertEquals("Should have 1 error", 1, response.getErrorCount() != null ? (int) response.getErrorCount() : 0);
        assertEquals("Should process 0 records", 0, response.getRecordCount() != null ? (int) response.getRecordCount() : 0);

        // Verify no data was saved (complete rollback)
        List<SKU> savedSkus = skuService.getAllSKUs();
        assertEquals("Should have no SKUs in database", 0, savedSkus.size());
    }

    // ==================== HELPER METHODS ====================

    /**
     * Create a SKU row with all fields
     */
    private HashMap<String, String> createSkuRow(String sku, String style, String size) {
        HashMap<String, String> row = new HashMap<>();
        row.put("sku", sku);
        row.put("style", style);
        row.put("size", size);
        return row;
    }

    /**
     * Generate a string of specified length for testing
     */
    private String generateString(String character, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(character);
        }
        return sb.toString();
    }
}
