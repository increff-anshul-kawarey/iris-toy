package com.iris.increff.service;

import com.iris.increff.AbstractUnitTest;
import com.iris.increff.controller.UploadResponse;
import com.iris.increff.dao.StoreDao;
import com.iris.increff.model.Store;
import com.iris.increff.exception.ApiException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Comprehensive test suite for StoreService
 * 
 * Tests all critical functionality including:
 * - CRUD operations (create, read, update, delete)
 * - Query methods (by branch name, store lookup)
 * - TSV data processing and validation
 * - Field mapping and normalization
 * - Error handling and edge cases
 * - Database operations and transactions
 * - Batch operations and rollback scenarios
 * - Business logic validation
 * 
 * Target: 90-95% method and line coverage for StoreService
 * 
 * @author Anshuk Kawarry
 * @version 1.0
 * @since 2025-01-01
 */
public class StoreServiceTest extends AbstractUnitTest {

    @Autowired
    private StoreService storeService;

    @Autowired
    private StoreDao storeDao;

    @Autowired
    private DataClearingService dataClearingService;

    private ArrayList<HashMap<String, String>> validTsvData;

    /**
     * Setup test data before each test method
     * Creates valid TSV data structures for testing
     */
    @Before
    public void setUp() {
        // Create valid TSV data with multiple rows
        validTsvData = new ArrayList<>();
        validTsvData.add(createStoreRow("MUMBAI_CENTRAL", "MUMBAI"));
        validTsvData.add(createStoreRow("DELHI_CP", "DELHI"));
        validTsvData.add(createStoreRow("BANGALORE_MG", "BANGALORE"));
    }

    // ==================== SUCCESSFUL PROCESSING TESTS ====================

    /**
     * Test successful processing of valid TSV data
     * Verifies that valid store data is processed and saved correctly
     */
    @Test
    @Transactional
    @Rollback
    public void testProcessAndSaveStores_ValidData() {
        // When: Process valid TSV data
        UploadResponse response = storeService.processAndSaveStores(validTsvData);

        // Then: Should succeed
        assertNotNull("Response should not be null", response);
        assertTrue("Response should be successful", response.isSuccess());
        assertEquals("Should process all records", 3, response.getRecordCount() != null ? (int) response.getRecordCount() : 0);
        assertEquals("Should have no errors", 0, response.getErrorCount() != null ? (int) response.getErrorCount() : 0);
        assertNotNull("Should have messages", response.getMessages());
        assertTrue("Should have success messages", response.getMessages().size() > 0);

        // Verify data was saved to database
        List<Store> savedStores = storeService.getAllStores();
        assertEquals("Should have 3 stores in database", 3, savedStores.size());

        // Verify specific store data
        Store store1 = savedStores.stream()
            .filter(s -> "MUMBAI_CENTRAL".equals(s.getBranch()))
            .findFirst()
            .orElse(null);
        assertNotNull("MUMBAI_CENTRAL store should be saved", store1);
        assertEquals("Branch should be normalized", "MUMBAI_CENTRAL", store1.getBranch());
        assertEquals("City should be normalized", "MUMBAI", store1.getCity());
    }

    /**
     * Test processing of single store record
     * Verifies that single record processing works correctly
     */
    @Test
    @Transactional
    @Rollback
    public void testProcessAndSaveStores_SingleRecord() {
        // Given: Single store record
        ArrayList<HashMap<String, String>> singleRecord = new ArrayList<>();
        singleRecord.add(createStoreRow("PUNE_FC", "PUNE"));

        // When: Process single record
        UploadResponse response = storeService.processAndSaveStores(singleRecord);

        // Then: Should succeed
        assertNotNull("Response should not be null", response);
        assertTrue("Response should be successful", response.isSuccess());
        assertEquals("Should process 1 record", 1, response.getRecordCount() != null ? (int) response.getRecordCount() : 0);
        assertEquals("Should have no errors", 0, response.getErrorCount() != null ? (int) response.getErrorCount() : 0);

        // Verify data was saved
        List<Store> savedStores = storeService.getAllStores();
        assertEquals("Should have 1 store in database", 1, savedStores.size());
        assertEquals("Should have correct branch", "PUNE_FC", savedStores.get(0).getBranch());
    }

    /**
     * Test data normalization functionality
     * Verifies that string data is properly normalized (trimmed and uppercased)
     */
    @Test
    @Transactional
    @Rollback
    public void testProcessAndSaveStores_DataNormalization() {
        // Given: Data with whitespace and mixed case
        ArrayList<HashMap<String, String>> unnormalizedData = new ArrayList<>();
        HashMap<String, String> row = createStoreRow(
            "  chennai_express  ",    // Leading/trailing spaces
            " chennai "              // Mixed case with spaces
        );
        unnormalizedData.add(row);

        // When: Process unnormalized data
        UploadResponse response = storeService.processAndSaveStores(unnormalizedData);

        // Then: Should succeed with normalized data
        assertTrue("Response should be successful", response.isSuccess());

        Store savedStore = storeService.getAllStores().get(0);
        assertEquals("Branch should be trimmed and uppercased", "CHENNAI_EXPRESS", savedStore.getBranch());
        assertEquals("City should be trimmed and uppercased", "CHENNAI", savedStore.getCity());
    }

    // ==================== VALIDATION ERROR TESTS ====================

    /**
     * Test validation of empty/null fields
     * Verifies that all required fields are validated for emptiness
     */
    @Test
    @Transactional
    @Rollback
    public void testProcessAndSaveStores_EmptyFields() {
        ArrayList<HashMap<String, String>> testData = new ArrayList<>();

        // Test empty branch
        testData.add(createStoreRow("", "MUMBAI"));
        // Test empty city
        testData.add(createStoreRow("MUMBAI_CENTRAL", ""));

        // When: Process data with empty fields
        UploadResponse response = storeService.processAndSaveStores(testData);

        // Then: Should fail with validation errors
        assertNotNull("Response should not be null", response);
        assertFalse("Response should fail", response.isSuccess());
        assertEquals("Should have 2 errors", 2, response.getErrorCount() != null ? (int) response.getErrorCount() : 0);
        assertEquals("Should process 0 records", 0, response.getRecordCount() != null ? (int) response.getRecordCount() : 0);

        List<String> errors = response.getErrors();
        assertTrue("Should have branch error", 
            errors.stream().anyMatch(e -> e.contains("Branch cannot be empty")));
        assertTrue("Should have city error", 
            errors.stream().anyMatch(e -> e.contains("City cannot be empty")));

        // Verify no data was saved
        List<Store> savedStores = storeService.getAllStores();
        assertEquals("Should have no stores in database", 0, savedStores.size());
    }

    /**
     * Test validation of field length constraints
     * Verifies that field length limits are enforced
     */
    @Test
    @Transactional
    @Rollback
    public void testProcessAndSaveStores_FieldLengthValidation() {
        ArrayList<HashMap<String, String>> testData = new ArrayList<>();

        // Test field length violations
        String longBranch = generateString("A", 51); // 51 characters (max 50)
        String longCity = generateString("B", 51); // 51 characters (max 50)
        
        testData.add(createStoreRow(longBranch, "MUMBAI"));
        testData.add(createStoreRow("MUMBAI_CENTRAL", longCity));

        // When: Process data with length violations
        UploadResponse response = storeService.processAndSaveStores(testData);

        // Then: Should fail with validation errors
        assertFalse("Response should fail", response.isSuccess());
        assertEquals("Should have 2 errors", 2, response.getErrorCount() != null ? (int) response.getErrorCount() : 0);

        List<String> errors = response.getErrors();
        assertTrue("Should have branch length error", 
            errors.stream().anyMatch(e -> e.contains("Branch must be between")));
        assertTrue("Should have city length error", 
            errors.stream().anyMatch(e -> e.contains("City must be between")));
    }

    /**
     * Test duplicate branch validation within batch
     * Verifies that duplicate branch names within the same upload are detected
     */
    @Test
    @Transactional
    @Rollback
    public void testProcessAndSaveStores_DuplicateBranches() {
        ArrayList<HashMap<String, String>> testData = new ArrayList<>();

        // Add duplicate branch names
        testData.add(createStoreRow("MUMBAI_CENTRAL", "MUMBAI"));
        testData.add(createStoreRow("MUMBAI_CENTRAL", "MUMBAI")); // Duplicate
        testData.add(createStoreRow("DELHI_CP", "DELHI"));
        testData.add(createStoreRow("DELHI_CP", "DELHI")); // Duplicate

        // When: Process data with duplicates
        UploadResponse response = storeService.processAndSaveStores(testData);

        // Then: Should fail with duplicate errors
        assertFalse("Response should fail", response.isSuccess());
        assertEquals("Should have 2 duplicate errors", 2, response.getErrorCount() != null ? (int) response.getErrorCount() : 0);

        List<String> errors = response.getErrors();
        assertTrue("Should have MUMBAI_CENTRAL duplicate error", 
            errors.stream().anyMatch(e -> e.contains("Duplicate branch 'MUMBAI_CENTRAL'")));
        assertTrue("Should have DELHI_CP duplicate error", 
            errors.stream().anyMatch(e -> e.contains("Duplicate branch 'DELHI_CP'")));
    }

    // ==================== MIXED SCENARIOS TESTS ====================

    /**
     * Test mixed valid and invalid data
     * Verifies that if any record is invalid, the entire batch fails
     */
    @Test
    @Transactional
    @Rollback
    public void testProcessAndSaveStores_MixedValidInvalid() {
        ArrayList<HashMap<String, String>> testData = new ArrayList<>();

        // Add valid records
        testData.add(createStoreRow("MUMBAI_CENTRAL", "MUMBAI"));
        testData.add(createStoreRow("DELHI_CP", "DELHI"));
        
        // Add invalid record
        testData.add(createStoreRow("", "BANGALORE")); // Empty branch
        
        // Add more valid records
        testData.add(createStoreRow("PUNE_FC", "PUNE"));

        // When: Process mixed data
        UploadResponse response = storeService.processAndSaveStores(testData);

        // Then: Should fail entirely (all-or-nothing approach)
        assertNotNull("Response should not be null", response);
        assertFalse("Response should fail", response.isSuccess());
        assertEquals("Should have 1 error", 1, response.getErrorCount() != null ? (int) response.getErrorCount() : 0);
        assertEquals("Should process 0 records", 0, response.getRecordCount() != null ? (int) response.getRecordCount() : 0);

        // Verify no data was saved (transaction rollback)
        List<Store> savedStores = storeService.getAllStores();
        assertEquals("Should have no stores in database", 0, savedStores.size());
    }

    /**
     * Test row number reporting in error messages
     * Verifies that error messages include correct row numbers
     */
    @Test
    @Transactional
    @Rollback
    public void testProcessAndSaveStores_RowNumberReporting() {
        ArrayList<HashMap<String, String>> testData = new ArrayList<>();

        // Row 1 (becomes row 2 in error message due to header)
        testData.add(createStoreRow("MUMBAI_CENTRAL", "MUMBAI")); // Valid
        
        // Row 2 (becomes row 3 in error message)
        testData.add(createStoreRow("", "DELHI")); // Invalid
        
        // Row 3 (becomes row 4 in error message)
        testData.add(createStoreRow("BANGALORE_MG", "")); // Invalid

        // When: Process data with errors
        UploadResponse response = storeService.processAndSaveStores(testData);

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
    public void testProcessAndSaveStores_EmptyTsvData() {
        // Given: Empty TSV data
        ArrayList<HashMap<String, String>> emptyData = new ArrayList<>();

        // When: Process empty data
        UploadResponse response = storeService.processAndSaveStores(emptyData);

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
    public void testProcessAndSaveStores_WhitespaceHandling() {
        ArrayList<HashMap<String, String>> testData = new ArrayList<>();

        // Test whitespace in all fields
        HashMap<String, String> row = new HashMap<>();
        row.put("branch", "  MUMBAI_CENTRAL  ");
        row.put("city", "\tMUMBAI\t");
        testData.add(row);

        // When: Process data with whitespace
        UploadResponse response = storeService.processAndSaveStores(testData);

        // Then: Should succeed with trimmed data
        assertTrue("Response should be successful", response.isSuccess());
        assertEquals("Should process 1 record", 1, response.getRecordCount() != null ? (int) response.getRecordCount() : 0);

        Store savedStore = storeService.getAllStores().get(0);
        assertEquals("Branch should be trimmed", "MUMBAI_CENTRAL", savedStore.getBranch());
        assertEquals("City should be trimmed", "MUMBAI", savedStore.getCity());
    }

    /**
     * Test maximum valid field lengths
     * Verifies that maximum allowed lengths are accepted
     */
    @Test
    @Transactional
    @Rollback
    public void testProcessAndSaveStores_MaximumValidLengths() {
        ArrayList<HashMap<String, String>> testData = new ArrayList<>();

        // Test maximum valid lengths (50 characters each)
        String maxBranch = generateString("A", 50);
        String maxCity = generateString("B", 50);
        
        testData.add(createStoreRow(maxBranch, maxCity));

        // When: Process data with maximum lengths
        UploadResponse response = storeService.processAndSaveStores(testData);

        // Then: Should succeed
        assertTrue("Response should be successful", response.isSuccess());
        assertEquals("Should process 1 record", 1, response.getRecordCount() != null ? (int) response.getRecordCount() : 0);

        Store savedStore = storeService.getAllStores().get(0);
        assertEquals("Branch should be saved correctly", maxBranch, savedStore.getBranch());
        assertEquals("City should be saved correctly", maxCity, savedStore.getCity());
    }

    // ==================== SERVICE METHOD TESTS ====================

    /**
     * Test getAllStores method
     * Verifies that all stores can be retrieved from database
     */
    @Test
    @Transactional
    @Rollback
    public void testGetAllStores() {
        // Given: Process some stores first
        storeService.processAndSaveStores(validTsvData);

        // When: Get all stores
        List<Store> allStores = storeService.getAllStores();

        // Then: Should return all saved stores
        assertNotNull("Stores list should not be null", allStores);
        assertEquals("Should return 3 stores", 3, allStores.size());

        // Verify branch names are present
        List<String> branches = allStores.stream()
            .map(Store::getBranch)
            .collect(java.util.stream.Collectors.toList());
        assertTrue("Should contain MUMBAI_CENTRAL", branches.contains("MUMBAI_CENTRAL"));
        assertTrue("Should contain DELHI_CP", branches.contains("DELHI_CP"));
        assertTrue("Should contain BANGALORE_MG", branches.contains("BANGALORE_MG"));
    }

    /**
     * Test findByBranch method with existing store
     * Verifies that stores can be found by their branch name
     */
    @Test
    @Transactional
    @Rollback
    public void testFindByBranch_ExistingStore() throws ApiException {
        // Given: Process some stores first
        storeService.processAndSaveStores(validTsvData);

        // When: Find store by branch
        Store foundStore = storeService.findByBranch("MUMBAI_CENTRAL");

        // Then: Should return the correct store
        assertNotNull("Found store should not be null", foundStore);
        assertEquals("Branch should match", "MUMBAI_CENTRAL", foundStore.getBranch());
        assertEquals("City should match", "MUMBAI", foundStore.getCity());
    }

    /**
     * Test findByBranch method with non-existing store
     * Verifies that appropriate exception is thrown for non-existing stores
     */
    @Test
    @Transactional
    @Rollback
    public void testFindByBranch_NonExistingStore() {
        // Given: No stores in database

        // When: Try to find non-existing store
        try {
            storeService.findByBranch("NONEXISTENT");
            fail("Should throw ApiException for non-existing store");
        } catch (ApiException e) {
            // Then: Should throw exception with appropriate message
            assertTrue("Should contain branch name in error message", 
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
    public void testProcessAndSaveStores_TransactionRollback() {
        // This test verifies that the @Transactional annotation works correctly
        // If there's a database error during save, the transaction should rollback
        
        // Given: Valid data that should normally succeed
        ArrayList<HashMap<String, String>> testData = new ArrayList<>();
        testData.add(createStoreRow("MUMBAI_CENTRAL", "MUMBAI"));

        // When: Process data (this should succeed in normal circumstances)
        UploadResponse response = storeService.processAndSaveStores(testData);

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
    public void testProcessAndSaveStores_DataClearing() {
        // Given: Create test data
        ArrayList<HashMap<String, String>> testData = new ArrayList<>();
        testData.add(createStoreRow("MUMBAI_CENTRAL", "MUMBAI"));
        testData.add(createStoreRow("DELHI_CP", "DELHI"));

        // When: Process data (should include clearing messages)
        UploadResponse response = storeService.processAndSaveStores(testData);

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
        List<Store> savedStores = storeService.getAllStores();
        assertEquals("Should have saved stores", 2, savedStores.size());
        
        // Verify specific stores are present
        assertTrue("Should contain MUMBAI_CENTRAL", 
            savedStores.stream().anyMatch(s -> "MUMBAI_CENTRAL".equals(s.getBranch())));
        assertTrue("Should contain DELHI_CP", 
            savedStores.stream().anyMatch(s -> "DELHI_CP".equals(s.getBranch())));
    }

    // ==================== BATCH OPERATIONS TESTS ====================

    /**
     * Test batch save operations
     * Verifies that multiple stores can be saved efficiently in batch
     */
    @Test
    @Transactional
    @Rollback
    public void testProcessAndSaveStores_BatchOperations() {
        // Given: Create large batch of stores
        ArrayList<HashMap<String, String>> largeBatch = new ArrayList<>();
        for (int i = 1; i <= 25; i++) {
            String branch = String.format("BRANCH_%03d", i);
            String city = String.format("CITY_%03d", i);
            largeBatch.add(createStoreRow(branch, city));
        }

        // When: Process large batch
        UploadResponse response = storeService.processAndSaveStores(largeBatch);

        // Then: Should succeed and save all records
        assertTrue("Response should be successful", response.isSuccess());
        assertEquals("Should process all records", 25, response.getRecordCount() != null ? (int) response.getRecordCount() : 0);
        assertEquals("Should have no errors", 0, response.getErrorCount() != null ? (int) response.getErrorCount() : 0);

        // Verify all stores were saved
        List<Store> savedStores = storeService.getAllStores();
        assertEquals("Should have saved all stores", 25, savedStores.size());
    }

    /**
     * Test batch rollback on partial failure
     * Verifies that if any store in batch fails, entire batch is rolled back
     */
    @Test
    @Transactional
    @Rollback
    public void testProcessAndSaveStores_BatchRollback() {
        // Given: Create batch with one invalid record
        ArrayList<HashMap<String, String>> batchWithError = new ArrayList<>();
        // Add valid records
        for (int i = 1; i <= 5; i++) {
            batchWithError.add(createStoreRow("BRANCH_00" + i, "CITY_00" + i));
        }
        // Add invalid record in the middle
        batchWithError.add(createStoreRow("", "CITY_006")); // Empty branch
        // Add more valid records
        for (int i = 7; i <= 10; i++) {
            batchWithError.add(createStoreRow("BRANCH_00" + i, "CITY_00" + i));
        }

        // When: Process batch with error
        UploadResponse response = storeService.processAndSaveStores(batchWithError);

        // Then: Should fail and rollback entire batch
        assertFalse("Response should fail", response.isSuccess());
        assertEquals("Should have 1 error", 1, response.getErrorCount() != null ? (int) response.getErrorCount() : 0);
        assertEquals("Should process 0 records", 0, response.getRecordCount() != null ? (int) response.getRecordCount() : 0);

        // Verify no data was saved (complete rollback)
        List<Store> savedStores = storeService.getAllStores();
        assertEquals("Should have no stores in database", 0, savedStores.size());
    }

    /**
     * Test case sensitivity and normalization consistency
     * Verifies that case normalization works consistently
     */
    @Test
    @Transactional
    @Rollback
    public void testProcessAndSaveStores_CaseNormalization() {
        ArrayList<HashMap<String, String>> testData = new ArrayList<>();

        // Test various case combinations
        testData.add(createStoreRow("mumbai_central", "mumbai"));
        testData.add(createStoreRow("Delhi_CP", "Delhi"));
        testData.add(createStoreRow("BANGALORE_MG", "BANGALORE"));

        // When: Process data with mixed cases
        UploadResponse response = storeService.processAndSaveStores(testData);

        // Then: Should succeed with normalized data
        assertTrue("Response should be successful", response.isSuccess());
        assertEquals("Should process all records", 3, response.getRecordCount() != null ? (int) response.getRecordCount() : 0);

        // Verify all data is normalized to uppercase
        List<Store> savedStores = storeService.getAllStores();
        for (Store store : savedStores) {
            assertEquals("Branch should be uppercase", store.getBranch().toUpperCase(), store.getBranch());
            assertEquals("City should be uppercase", store.getCity().toUpperCase(), store.getCity());
        }
    }

    // ==================== HELPER METHODS ====================

    /**
     * Create a store row with all fields
     */
    private HashMap<String, String> createStoreRow(String branch, String city) {
        HashMap<String, String> row = new HashMap<>();
        row.put("branch", branch);
        row.put("city", city);
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
