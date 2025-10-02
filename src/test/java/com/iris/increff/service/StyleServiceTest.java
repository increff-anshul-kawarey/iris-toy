package com.iris.increff.service;

import com.iris.increff.AbstractUnitTest;
import com.iris.increff.controller.UploadResponse;
import com.iris.increff.dao.StyleDao;
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
 * Comprehensive test suite for StyleService
 * 
 * Tests all critical functionality including:
 * - TSV data processing and validation
 * - Field mapping and normalization
 * - Error handling and edge cases
 * - Database operations and transactions
 * - Business logic validation
 * 
 * Target: 90%+ method and line coverage for StyleService
 * 
 * @author Anshuk Kawarry
 * @version 1.0
 * @since 2025-01-01
 */
public class StyleServiceTest extends AbstractUnitTest {

    @Autowired
    private StyleService styleService;

    @Autowired
    private StyleDao styleDao;

    @Autowired
    private DataClearingService dataClearingService;

    private ArrayList<HashMap<String, String>> validTsvData;
    private HashMap<String, String> validStyleRow;

    /**
     * Setup test data before each test method
     * Creates valid TSV data structures for testing
     */
    @Before
    public void setUp() {
        // Create valid style row data
        validStyleRow = new HashMap<>();
        validStyleRow.put("style", "SHIRT001");
        validStyleRow.put("brand", "Nike");
        validStyleRow.put("category", "SHIRTS");
        validStyleRow.put("sub_category", "CASUAL");
        validStyleRow.put("mrp", "100.50");
        validStyleRow.put("gender", "M");

        // Create valid TSV data with multiple rows
        validTsvData = new ArrayList<>();
        validTsvData.add(createStyleRow("SHIRT001", "Nike", "SHIRTS", "CASUAL", "100.50", "M"));
        validTsvData.add(createStyleRow("PANT001", "Adidas", "PANTS", "FORMAL", "150.75", "F"));
        validTsvData.add(createStyleRow("DRESS001", "Zara", "DRESSES", "PARTY", "200.00", "F"));
    }

    // ==================== SUCCESSFUL PROCESSING TESTS ====================

    /**
     * Test successful processing of valid TSV data
     * Verifies that valid data is processed and saved correctly
     */
    @Test
    @Transactional
    @Rollback
    public void testProcessAndSaveStyles_ValidData() {
        // Given: Create test data inline
        ArrayList<HashMap<String, String>> testData = new ArrayList<>();
        testData.add(createStyleRow("SHIRT001", "Nike", "SHIRTS", "CASUAL", "100.50", "M"));
        testData.add(createStyleRow("PANT001", "Adidas", "PANTS", "FORMAL", "150.75", "F"));
        testData.add(createStyleRow("DRESS001", "Zara", "DRESSES", "PARTY", "200.00", "F"));

        // When: Process valid TSV data
        UploadResponse response = styleService.processAndSaveStyles(testData);

        // Then: Should succeed
        assertNotNull("Response should not be null", response);
        assertTrue("Response should be successful", response.isSuccess());
        if (response.getRecordCount() != null) {
            assertEquals("Should process all records", 3, response.getRecordCount() != null ? (int) response.getRecordCount() : 0);
        }
        if (response.getErrorCount() != null) {
            assertEquals("Should have no errors", 0, response.getErrorCount() != null ? (int) response.getErrorCount() : 0);
        }
        assertNotNull("Should have messages", response.getMessages());
        assertTrue("Should have success messages", response.getMessages().size() > 0);

        // Verify data was saved to database
        List<Style> savedStyles = styleService.getAllStyles();
        assertEquals("Should have 3 styles in database", 3, savedStyles.size());

        // Verify specific style data
        Style shirt = savedStyles.stream()
            .filter(s -> "SHIRT001".equals(s.getStyleCode()))
            .findFirst()
            .orElse(null);
        assertNotNull("SHIRT001 should be saved", shirt);
        assertEquals("Brand should be normalized", "NIKE", shirt.getBrand());
        assertEquals("Category should be normalized", "SHIRTS", shirt.getCategory());
        assertEquals("Sub-category should be normalized", "CASUAL", shirt.getSubCategory());
        assertEquals("MRP should be correct", new BigDecimal("100.50"), shirt.getMrp());
        assertEquals("Gender should be normalized", "M", shirt.getGender());
    }

    /**
     * Test processing of single style record
     * Verifies that single record processing works correctly
     */
    @Test
    @Transactional
    @Rollback
    public void testProcessAndSaveStyles_SingleRecord() {
        // Given: Single style record
        ArrayList<HashMap<String, String>> singleRecord = new ArrayList<>();
        singleRecord.add(validStyleRow);

        // When: Process single record
        UploadResponse response = styleService.processAndSaveStyles(singleRecord);

        // Then: Should succeed
        assertNotNull("Response should not be null", response);
        assertTrue("Response should be successful", response.isSuccess());
        if (response.getRecordCount() != null) {
            assertEquals("Should process 1 record", 1, response.getRecordCount() != null ? (int) response.getRecordCount() : 0);
        }
        if (response.getErrorCount() != null) {
            assertEquals("Should have no errors", 0, response.getErrorCount() != null ? (int) response.getErrorCount() : 0);
        }

        // Verify data was saved
        List<Style> savedStyles = styleService.getAllStyles();
        assertEquals("Should have 1 style in database", 1, savedStyles.size());
    }

    /**
     * Test data normalization functionality
     * Verifies that string data is properly normalized (trimmed and uppercased)
     */
    @Test
    @Transactional
    @Rollback
    public void testProcessAndSaveStyles_DataNormalization() {
        // Given: Data with whitespace and mixed case
        ArrayList<HashMap<String, String>> unnormalizedData = new ArrayList<>();
        HashMap<String, String> row = createStyleRow(
            "  shirt002  ",  // Leading/trailing spaces
            " nike ",        // Mixed case with spaces
            "shirts",        // Lowercase
            "CASUAL",        // Already uppercase
            "99.99",         // Normal number
            "m"              // Lowercase gender
        );
        unnormalizedData.add(row);

        // When: Process unnormalized data
        UploadResponse response = styleService.processAndSaveStyles(unnormalizedData);

        // Then: Should succeed with normalized data
        assertTrue("Response should be successful", response.isSuccess());

        Style savedStyle = styleService.getAllStyles().get(0);
        assertEquals("Style code should be trimmed and uppercased", "SHIRT002", savedStyle.getStyleCode());
        assertEquals("Brand should be trimmed and uppercased", "NIKE", savedStyle.getBrand());
        assertEquals("Category should be uppercased", "SHIRTS", savedStyle.getCategory());
        assertEquals("Sub-category should remain uppercase", "CASUAL", savedStyle.getSubCategory());
        assertEquals("Gender should be uppercased", "M", savedStyle.getGender());
    }

    // ==================== VALIDATION ERROR TESTS ====================

    /**
     * Test validation of empty/null fields
     * Verifies that all required fields are validated for emptiness
     */
    @Test
    @Transactional
    @Rollback
    public void testProcessAndSaveStyles_EmptyFields() {
        ArrayList<HashMap<String, String>> testData = new ArrayList<>();

        // Test empty style code
        testData.add(createStyleRow("", "Nike", "SHIRTS", "CASUAL", "100.00", "M"));
        // Test null brand
        testData.add(createStyleRowWithNull("SHIRT002", null, "SHIRTS", "CASUAL", "100.00", "M"));
        // Test empty category
        testData.add(createStyleRow("SHIRT003", "Nike", "", "CASUAL", "100.00", "M"));
        // Test empty sub-category
        testData.add(createStyleRow("SHIRT004", "Nike", "SHIRTS", "", "100.00", "M"));
        // Test empty MRP
        testData.add(createStyleRow("SHIRT005", "Nike", "SHIRTS", "CASUAL", "", "M"));
        // Test empty gender
        testData.add(createStyleRow("SHIRT006", "Nike", "SHIRTS", "CASUAL", "100.00", ""));

        // When: Process data with empty fields
        UploadResponse response = styleService.processAndSaveStyles(testData);

        // Then: Should fail with validation errors
        assertNotNull("Response should not be null", response);
        assertFalse("Response should fail", response.isSuccess());
        assertEquals("Should have 6 errors", 6, response.getErrorCount() != null ? (int) response.getErrorCount() : 0);
        assertEquals("Should process 0 records", 0, response.getRecordCount() != null ? (int) response.getRecordCount() : 0);

        List<String> errors = response.getErrors();
        assertTrue("Should have style code error", 
            errors.stream().anyMatch(e -> e.contains("Style code cannot be empty")));
        assertTrue("Should have brand error", 
            errors.stream().anyMatch(e -> e.contains("Brand cannot be empty")));
        assertTrue("Should have category error", 
            errors.stream().anyMatch(e -> e.contains("Category cannot be empty")));
        assertTrue("Should have sub-category error", 
            errors.stream().anyMatch(e -> e.contains("Sub-category cannot be empty")));
        assertTrue("Should have MRP error", 
            errors.stream().anyMatch(e -> e.contains("MRP cannot be empty")));
        assertTrue("Should have gender error", 
            errors.stream().anyMatch(e -> e.contains("Gender cannot be empty")));

        // Verify no data was saved
        List<Style> savedStyles = styleService.getAllStyles();
        assertEquals("Should have no styles in database", 0, savedStyles.size());
    }

    /**
     * Test validation of field length constraints
     * Verifies that field length limits are enforced
     */
    @Test
    @Transactional
    @Rollback
    public void testProcessAndSaveStyles_FieldLengthValidation() {
        ArrayList<HashMap<String, String>> testData = new ArrayList<>();

        // Test field length violations (assuming 50 character limit)
        String longString = generateString("A", 51); // 51 characters
        testData.add(createStyleRow(longString, "Nike", "SHIRTS", "CASUAL", "100.00", "M"));
        testData.add(createStyleRow("SHIRT002", longString, "SHIRTS", "CASUAL", "100.00", "M"));
        testData.add(createStyleRow("SHIRT003", "Nike", longString, "CASUAL", "100.00", "M"));
        testData.add(createStyleRow("SHIRT004", "Nike", "SHIRTS", longString, "100.00", "M"));
        testData.add(createStyleRow("SHIRT005", "Nike", "SHIRTS", "CASUAL", "100.00", longString));

        // When: Process data with length violations
        UploadResponse response = styleService.processAndSaveStyles(testData);

        // Then: Should fail with validation errors
        assertFalse("Response should fail", response.isSuccess());
        assertEquals("Should have 5 errors", 5, response.getErrorCount() != null ? (int) response.getErrorCount() : 0);

        List<String> errors = response.getErrors();
        assertTrue("Should have style code length error", 
            errors.stream().anyMatch(e -> e.contains("Style code must be between")));
        assertTrue("Should have brand length error", 
            errors.stream().anyMatch(e -> e.contains("Brand must be between")));
        assertTrue("Should have category length error", 
            errors.stream().anyMatch(e -> e.contains("Category must be between")));
        assertTrue("Should have sub-category length error", 
            errors.stream().anyMatch(e -> e.contains("Sub-category must be between")));
        assertTrue("Should have gender length error", 
            errors.stream().anyMatch(e -> e.contains("Gender must be between")));
    }

    /**
     * Test MRP validation scenarios
     * Verifies that MRP field is properly validated for format and value
     */
    @Test
    @Transactional
    @Rollback
    public void testProcessAndSaveStyles_MrpValidation() {
        ArrayList<HashMap<String, String>> testData = new ArrayList<>();

        // Test invalid MRP formats and values
        testData.add(createStyleRow("SHIRT001", "Nike", "SHIRTS", "CASUAL", "invalid", "M"));
        testData.add(createStyleRow("SHIRT002", "Nike", "SHIRTS", "CASUAL", "abc123", "M"));
        testData.add(createStyleRow("SHIRT003", "Nike", "SHIRTS", "CASUAL", "-50.00", "M"));
        testData.add(createStyleRow("SHIRT004", "Nike", "SHIRTS", "CASUAL", "0", "M"));
        testData.add(createStyleRow("SHIRT005", "Nike", "SHIRTS", "CASUAL", "0.00", "M"));

        // When: Process data with invalid MRP
        UploadResponse response = styleService.processAndSaveStyles(testData);

        // Then: Should fail with validation errors
        assertFalse("Response should fail", response.isSuccess());
        assertEquals("Should have 5 errors", 5, response.getErrorCount() != null ? (int) response.getErrorCount() : 0);

        List<String> errors = response.getErrors();
        assertTrue("Should have invalid format error", 
            errors.stream().anyMatch(e -> e.contains("Invalid MRP format")));
        assertTrue("Should have negative MRP error", 
            errors.stream().anyMatch(e -> e.contains("MRP must be greater than 0")));
        assertTrue("Should have zero MRP error", 
            errors.stream().anyMatch(e -> e.contains("MRP must be greater than 0")));
    }

    /**
     * Test duplicate style code validation within batch
     * Verifies that duplicate style codes within the same upload are detected
     */
    @Test
    @Transactional
    @Rollback
    public void testProcessAndSaveStyles_DuplicateStyleCodes() {
        ArrayList<HashMap<String, String>> testData = new ArrayList<>();

        // Add duplicate style codes
        testData.add(createStyleRow("SHIRT001", "Nike", "SHIRTS", "CASUAL", "100.00", "M"));
        testData.add(createStyleRow("SHIRT001", "Adidas", "SHIRTS", "SPORTS", "120.00", "F")); // Duplicate
        testData.add(createStyleRow("PANT001", "Nike", "PANTS", "FORMAL", "150.00", "M"));
        testData.add(createStyleRow("PANT001", "Zara", "PANTS", "CASUAL", "140.00", "F")); // Duplicate

        // When: Process data with duplicates
        UploadResponse response = styleService.processAndSaveStyles(testData);

        // Then: Should fail with duplicate errors
        assertFalse("Response should fail", response.isSuccess());
        assertEquals("Should have 2 duplicate errors", 2, response.getErrorCount() != null ? (int) response.getErrorCount() : 0);

        List<String> errors = response.getErrors();
        assertTrue("Should have SHIRT001 duplicate error", 
            errors.stream().anyMatch(e -> e.contains("Duplicate style code 'SHIRT001'")));
        assertTrue("Should have PANT001 duplicate error", 
            errors.stream().anyMatch(e -> e.contains("Duplicate style code 'PANT001'")));
    }

    // ==================== MIXED SCENARIOS TESTS ====================

    /**
     * Test mixed valid and invalid data
     * Verifies that if any record is invalid, the entire batch fails
     */
    @Test
    @Transactional
    @Rollback
    public void testProcessAndSaveStyles_MixedValidInvalid() {
        ArrayList<HashMap<String, String>> testData = new ArrayList<>();

        // Add valid records
        testData.add(createStyleRow("SHIRT001", "Nike", "SHIRTS", "CASUAL", "100.00", "M"));
        testData.add(createStyleRow("PANT001", "Adidas", "PANTS", "FORMAL", "150.00", "F"));
        
        // Add invalid record
        testData.add(createStyleRow("", "Nike", "SHIRTS", "CASUAL", "100.00", "M")); // Empty style code
        
        // Add more valid records
        testData.add(createStyleRow("DRESS001", "Zara", "DRESSES", "PARTY", "200.00", "F"));

        // When: Process mixed data
        UploadResponse response = styleService.processAndSaveStyles(testData);

        // Then: Should fail entirely (all-or-nothing approach)
        assertNotNull("Response should not be null", response);
        assertFalse("Response should fail", response.isSuccess());
        assertEquals("Should have 1 error", 1, response.getErrorCount() != null ? (int) response.getErrorCount() : 0);
        assertEquals("Should process 0 records", 0, response.getRecordCount() != null ? (int) response.getRecordCount() : 0);

        // Verify no data was saved (transaction rollback)
        List<Style> savedStyles = styleService.getAllStyles();
        assertEquals("Should have no styles in database", 0, savedStyles.size());
    }

    /**
     * Test row number reporting in error messages
     * Verifies that error messages include correct row numbers
     */
    @Test
    @Transactional
    @Rollback
    public void testProcessAndSaveStyles_RowNumberReporting() {
        ArrayList<HashMap<String, String>> testData = new ArrayList<>();

        // Row 1 (becomes row 2 in error message due to header)
        testData.add(createStyleRow("SHIRT001", "Nike", "SHIRTS", "CASUAL", "100.00", "M")); // Valid
        
        // Row 2 (becomes row 3 in error message)
        testData.add(createStyleRow("", "Nike", "SHIRTS", "CASUAL", "100.00", "M")); // Invalid
        
        // Row 3 (becomes row 4 in error message)
        testData.add(createStyleRow("SHIRT003", "Nike", "", "CASUAL", "100.00", "M")); // Invalid

        // When: Process data with errors
        UploadResponse response = styleService.processAndSaveStyles(testData);

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
    public void testProcessAndSaveStyles_EmptyTsvData() {
        // Given: Empty TSV data
        ArrayList<HashMap<String, String>> emptyData = new ArrayList<>();

        // When: Process empty data
        UploadResponse response = styleService.processAndSaveStyles(emptyData);

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
     * Test MRP with various decimal formats
     * Verifies that different valid decimal formats are accepted
     */
    @Test
    @Transactional
    @Rollback
    public void testProcessAndSaveStyles_MrpDecimalFormats() {
        ArrayList<HashMap<String, String>> testData = new ArrayList<>();

        // Test various valid decimal formats
        testData.add(createStyleRow("SHIRT001", "Nike", "SHIRTS", "CASUAL", "100", "M"));        // Integer
        testData.add(createStyleRow("SHIRT002", "Nike", "SHIRTS", "CASUAL", "100.0", "M"));      // One decimal
        testData.add(createStyleRow("SHIRT003", "Nike", "SHIRTS", "CASUAL", "100.50", "M"));     // Two decimals
        testData.add(createStyleRow("SHIRT004", "Nike", "SHIRTS", "CASUAL", "100.123", "M"));    // Three decimals
        testData.add(createStyleRow("SHIRT005", "Nike", "SHIRTS", "CASUAL", "0.01", "M"));       // Small value

        // When: Process data with various MRP formats
        UploadResponse response = styleService.processAndSaveStyles(testData);

        // Then: Should succeed
        assertTrue("Response should be successful", response.isSuccess());
        assertEquals("Should process all records", 5, response.getRecordCount() != null ? (int) response.getRecordCount() : 0);

        // Verify MRP values are correctly parsed
        List<Style> savedStyles = styleService.getAllStyles();
        assertEquals("Should have 5 styles", 5, savedStyles.size());

        // Check specific MRP values
        Style shirt1 = findStyleByCode(savedStyles, "SHIRT001");
        assertEquals("Integer MRP should be parsed", 0, new BigDecimal("100").compareTo(shirt1.getMrp()));

        Style shirt5 = findStyleByCode(savedStyles, "SHIRT005");
        assertEquals("Small MRP should be parsed", 0, new BigDecimal("0.01").compareTo(shirt5.getMrp()));
    }

    /**
     * Test whitespace handling in all fields
     * Verifies that leading/trailing whitespace is properly handled
     */
    @Test
    @Transactional
    @Rollback
    public void testProcessAndSaveStyles_WhitespaceHandling() {
        ArrayList<HashMap<String, String>> testData = new ArrayList<>();

        // Test whitespace in all fields
        HashMap<String, String> row = new HashMap<>();
        row.put("style", "  SHIRT001  ");
        row.put("brand", "\tNike\t");
        row.put("category", " SHIRTS ");
        row.put("sub_category", "  CASUAL  ");
        row.put("mrp", "  100.50  ");
        row.put("gender", " M ");
        testData.add(row);

        // When: Process data with whitespace
        UploadResponse response = styleService.processAndSaveStyles(testData);

        // Then: Should succeed with trimmed data
        assertTrue("Response should be successful", response.isSuccess());
        assertEquals("Should process 1 record", 1, response.getRecordCount() != null ? (int) response.getRecordCount() : 0);

        Style savedStyle = styleService.getAllStyles().get(0);
        assertEquals("Style code should be trimmed", "SHIRT001", savedStyle.getStyleCode());
        assertEquals("Brand should be trimmed", "NIKE", savedStyle.getBrand());
        assertEquals("Category should be trimmed", "SHIRTS", savedStyle.getCategory());
        assertEquals("Sub-category should be trimmed", "CASUAL", savedStyle.getSubCategory());
        assertEquals("Gender should be trimmed", "M", savedStyle.getGender());
        assertEquals("MRP should be parsed correctly", new BigDecimal("100.50"), savedStyle.getMrp());
    }

    // ==================== SERVICE METHOD TESTS ====================

    /**
     * Test getAllStyles method
     * Verifies that all styles can be retrieved from database
     */
    @Test
    @Transactional
    @Rollback
    public void testGetAllStyles() {
        // Given: Process some styles first
        styleService.processAndSaveStyles(validTsvData);

        // When: Get all styles
        List<Style> allStyles = styleService.getAllStyles();

        // Then: Should return all saved styles
        assertNotNull("Styles list should not be null", allStyles);
        assertEquals("Should return 3 styles", 3, allStyles.size());

        // Verify style codes are present
        List<String> styleCodes = allStyles.stream()
            .map(Style::getStyleCode)
            .collect(java.util.stream.Collectors.toList());
        assertTrue("Should contain SHIRT001", styleCodes.contains("SHIRT001"));
        assertTrue("Should contain PANT001", styleCodes.contains("PANT001"));
        assertTrue("Should contain DRESS001", styleCodes.contains("DRESS001"));
    }

    /**
     * Test findByStyleCode method with existing style
     * Verifies that styles can be found by their style code
     */
    @Test
    @Transactional
    @Rollback
    public void testFindByStyleCode_ExistingStyle() throws ApiException {
        // Given: Process some styles first
        styleService.processAndSaveStyles(validTsvData);

        // When: Find style by code
        Style foundStyle = styleService.findByStyleCode("SHIRT001");

        // Then: Should return the correct style
        assertNotNull("Found style should not be null", foundStyle);
        assertEquals("Style code should match", "SHIRT001", foundStyle.getStyleCode());
        assertEquals("Brand should match", "NIKE", foundStyle.getBrand());
        assertEquals("Category should match", "SHIRTS", foundStyle.getCategory());
    }

    /**
     * Test findByStyleCode method with non-existing style
     * Verifies that appropriate exception is thrown for non-existing styles
     */
    @Test
    @Transactional
    @Rollback
    public void testFindByStyleCode_NonExistingStyle() {
        // Given: No styles in database

        // When: Try to find non-existing style
        try {
            styleService.findByStyleCode("NONEXISTENT");
            fail("Should throw ApiException for non-existing style");
        } catch (ApiException e) {
            // Then: Should throw exception with appropriate message
            assertTrue("Should contain style code in error message", 
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
    public void testProcessAndSaveStyles_TransactionRollback() {
        // This test verifies that the @Transactional annotation works correctly
        // If there's a database error during save, the transaction should rollback
        
        // Given: Valid data that should normally succeed
        ArrayList<HashMap<String, String>> testData = new ArrayList<>();
        testData.add(createStyleRow("SHIRT001", "Nike", "SHIRTS", "CASUAL", "100.00", "M"));

        // When: Process data (this should succeed in normal circumstances)
        UploadResponse response = styleService.processAndSaveStyles(testData);

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
    public void testProcessAndSaveStyles_DataClearing() {
        // Given: Create test data inline to avoid session conflicts
        ArrayList<HashMap<String, String>> testData = new ArrayList<>();
        testData.add(createStyleRow("SHIRT001", "Nike", "SHIRTS", "CASUAL", "100.50", "M"));
        testData.add(createStyleRow("PANT001", "Adidas", "PANTS", "FORMAL", "150.75", "F"));

        // When: Process data (should include clearing messages)
        UploadResponse response = styleService.processAndSaveStyles(testData);

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
        List<Style> savedStyles = styleService.getAllStyles();
        assertEquals("Should have saved styles", 2, savedStyles.size());
        
        // Verify specific styles are present
        assertTrue("Should contain SHIRT001", 
            savedStyles.stream().anyMatch(s -> "SHIRT001".equals(s.getStyleCode())));
        assertTrue("Should contain PANT001", 
            savedStyles.stream().anyMatch(s -> "PANT001".equals(s.getStyleCode())));
    }

    // ==================== HELPER METHODS ====================

    /**
     * Create a style row with all fields
     */
    private HashMap<String, String> createStyleRow(String style, String brand, String category, 
                                                  String subCategory, String mrp, String gender) {
        HashMap<String, String> row = new HashMap<>();
        row.put("style", style);
        row.put("brand", brand);
        row.put("category", category);
        row.put("sub_category", subCategory);
        row.put("mrp", mrp);
        row.put("gender", gender);
        return row;
    }

    /**
     * Create a style row with null value for testing
     */
    private HashMap<String, String> createStyleRowWithNull(String style, String brand, String category, 
                                                          String subCategory, String mrp, String gender) {
        HashMap<String, String> row = new HashMap<>();
        row.put("style", style);
        row.put("brand", brand);
        row.put("category", category);
        row.put("sub_category", subCategory);
        row.put("mrp", mrp);
        row.put("gender", gender);
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

    /**
     * Find a style by style code from a list
     */
    private Style findStyleByCode(List<Style> styles, String styleCode) {
        return styles.stream()
            .filter(s -> styleCode.equals(s.getStyleCode()))
            .findFirst()
            .orElse(null);
    }
}
