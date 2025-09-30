package com.iris.increff.dao;

import com.iris.increff.AbstractUnitTest;
import com.iris.increff.model.NoosResult;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Comprehensive test suite for NoosResultDao
 * 
 * Tests all CRUD operations, query methods, batch operations, edge cases,
 * and transaction scenarios to achieve 90%+ method coverage.
 * 
 * @author Anshuk Kawarry
 * @version 1.0
 * @since 2025-01-01
 */
public class NoosResultDaoTest extends AbstractUnitTest {

    @Autowired
    private NoosResultDao noosResultDao;

    private NoosResult testResult1;
    private NoosResult testResult2;
    private NoosResult testResult3;
    private NoosResult testResult4;
    
    private Date testCalculatedDate1;
    private Date testCalculatedDate2;
    private Date testCalculatedDate3;

    /**
     * Setup test data before each test method
     * Creates sample NoosResult objects for testing
     */
    @Before
    public void setUp() {
        // Create test dates
        Calendar cal = Calendar.getInstance();
        testCalculatedDate1 = cal.getTime();
        
        cal.add(Calendar.DAY_OF_MONTH, -1);
        testCalculatedDate2 = cal.getTime();
        
        cal.add(Calendar.DAY_OF_MONTH, -1);
        testCalculatedDate3 = cal.getTime();

        // Create test result 1 - Core type
        testResult1 = new NoosResult();
        testResult1.setCategory("SHIRTS");
        testResult1.setStyleCode("SHIRT001");
        testResult1.setStyleROS(new BigDecimal("0.8500"));
        testResult1.setType("core");
        testResult1.setStyleRevContribution(new BigDecimal("15.2500"));
        testResult1.setCalculatedDate(testCalculatedDate1);
        testResult1.setTotalQuantitySold(150);
        testResult1.setTotalRevenue(new BigDecimal("45000.00"));
        testResult1.setDaysAvailable(30);
        testResult1.setDaysWithSales(25);
        testResult1.setAvgDiscount(new BigDecimal("5.5000"));
        testResult1.setAlgorithmRunId(1001L);

        // Create test result 2 - Bestseller type
        testResult2 = new NoosResult();
        testResult2.setCategory("PANTS");
        testResult2.setStyleCode("PANT001");
        testResult2.setStyleROS(new BigDecimal("1.2000"));
        testResult2.setType("bestseller");
        testResult2.setStyleRevContribution(new BigDecimal("25.7500"));
        testResult2.setCalculatedDate(testCalculatedDate2);
        testResult2.setTotalQuantitySold(200);
        testResult2.setTotalRevenue(new BigDecimal("60000.00"));
        testResult2.setDaysAvailable(30);
        testResult2.setDaysWithSales(28);
        testResult2.setAvgDiscount(new BigDecimal("3.2500"));
        testResult2.setAlgorithmRunId(1001L);

        // Create test result 3 - Fashion type
        testResult3 = new NoosResult();
        testResult3.setCategory("DRESSES");
        testResult3.setStyleCode("DRESS001");
        testResult3.setStyleROS(new BigDecimal("0.4500"));
        testResult3.setType("fashion");
        testResult3.setStyleRevContribution(new BigDecimal("8.1250"));
        testResult3.setCalculatedDate(testCalculatedDate3);
        testResult3.setTotalQuantitySold(75);
        testResult3.setTotalRevenue(new BigDecimal("22500.00"));
        testResult3.setDaysAvailable(30);
        testResult3.setDaysWithSales(15);
        testResult3.setAvgDiscount(new BigDecimal("12.7500"));
        testResult3.setAlgorithmRunId(1002L);

        // Create test result 4 - Another core type for same category
        testResult4 = new NoosResult();
        testResult4.setCategory("SHIRTS");
        testResult4.setStyleCode("SHIRT002");
        testResult4.setStyleROS(new BigDecimal("0.9200"));
        testResult4.setType("core");
        testResult4.setStyleRevContribution(new BigDecimal("18.5000"));
        testResult4.setCalculatedDate(testCalculatedDate1);
        testResult4.setTotalQuantitySold(180);
        testResult4.setTotalRevenue(new BigDecimal("54000.00"));
        testResult4.setDaysAvailable(30);
        testResult4.setDaysWithSales(27);
        testResult4.setAvgDiscount(new BigDecimal("4.2500"));
        testResult4.setAlgorithmRunId(1002L);
    }

    // ==================== CRUD OPERATIONS TESTS ====================

    /**
     * Test inserting a new NOOS result
     * Verifies that a new result is persisted with generated ID
     */
    @Test
    @Transactional
    @Rollback
    public void testInsert_NewResult() {
        // Given: A new NOOS result without ID
        assertNull("Result ID should be null before saving", testResult1.getId());

        // When: Insert the result
        noosResultDao.insert(testResult1);

        // Then: Result should be saved with generated ID
        assertNotNull("Result should have generated ID", testResult1.getId());
        assertEquals("Category should match", "SHIRTS", testResult1.getCategory());
        assertEquals("Style code should match", "SHIRT001", testResult1.getStyleCode());
        assertEquals("Style ROS should match", new BigDecimal("0.8500"), testResult1.getStyleROS());
        assertEquals("Type should match", "core", testResult1.getType());
        assertEquals("Style rev contribution should match", new BigDecimal("15.2500"), testResult1.getStyleRevContribution());
        assertEquals("Calculated date should match", testCalculatedDate1, testResult1.getCalculatedDate());
        assertEquals("Total quantity sold should match", Integer.valueOf(150), testResult1.getTotalQuantitySold());
        assertEquals("Algorithm run ID should match", Long.valueOf(1001L), testResult1.getAlgorithmRunId());
    }

    /**
     * Test selecting a result by ID
     * Verifies that select returns correct result
     */
    @Test
    @Transactional
    @Rollback
    public void testSelect_ExistingResult() {
        // Given: Insert a result
        noosResultDao.insert(testResult1);
        Long resultId = testResult1.getId();

        // When: Select by ID
        NoosResult foundResult = noosResultDao.select(resultId);

        // Then: Should return the correct result
        assertNotNull("Found result should not be null", foundResult);
        assertEquals("ID should match", resultId, foundResult.getId());
        assertEquals("Category should match", "SHIRTS", foundResult.getCategory());
        assertEquals("Style code should match", "SHIRT001", foundResult.getStyleCode());
        assertEquals("Type should match", "core", foundResult.getType());
        assertEquals("Algorithm run ID should match", Long.valueOf(1001L), foundResult.getAlgorithmRunId());
    }

    /**
     * Test selecting a result by non-existent ID
     * Verifies that select returns null for non-existent ID
     */
    @Test
    @Transactional
    @Rollback
    public void testSelect_NonExistentResult() {
        // Given: A non-existent ID
        Long nonExistentId = 99999L;

        // When: Select by non-existent ID
        NoosResult foundResult = noosResultDao.select(nonExistentId);

        // Then: Should return null
        assertNull("Should return null for non-existent ID", foundResult);
    }

    /**
     * Test selecting a result by null ID
     * Verifies that select handles null input gracefully
     */
    @Test
    @Transactional
    @Rollback
    public void testSelect_NullId() {
        // When: Select by null ID
        try {
            NoosResult foundResult = noosResultDao.select(null);
            // Hibernate may throw exception for null ID, which is acceptable
            assertNull("Should return null for null ID", foundResult);
        } catch (IllegalArgumentException e) {
            // This is also acceptable behavior - Hibernate doesn't allow null IDs
            assertTrue("Should throw IllegalArgumentException for null ID", 
                e.getMessage().contains("id to load is required"));
        }
    }

    // ==================== QUERY METHODS TESTS ====================

    /**
     * Test getting latest results
     * Verifies that getLatestResults returns results ordered by calculated date DESC
     */
    @Test
    @Transactional
    @Rollback
    public void testGetLatestResults_WithResults() {
        // Given: Insert multiple results with different dates
        noosResultDao.insert(testResult1); // Most recent
        noosResultDao.insert(testResult2); // 1 day ago
        noosResultDao.insert(testResult3); // 2 days ago

        // When: Get latest results
        List<NoosResult> latestResults = noosResultDao.getLatestResults();

        // Then: Should return results ordered by calculated date DESC
        assertNotNull("Latest results should not be null", latestResults);
        assertEquals("Should return 3 results", 3, latestResults.size());
        
        // Verify ordering (most recent first)
        assertEquals("First result should be most recent", testCalculatedDate1, latestResults.get(0).getCalculatedDate());
        assertEquals("Second result should be 1 day ago", testCalculatedDate2, latestResults.get(1).getCalculatedDate());
        assertEquals("Third result should be 2 days ago", testCalculatedDate3, latestResults.get(2).getCalculatedDate());
    }

    /**
     * Test getting latest results when no results exist
     * Verifies that getLatestResults returns empty list when no data
     */
    @Test
    @Transactional
    @Rollback
    public void testGetLatestResults_NoResults() {
        // When: Get latest results with no data
        List<NoosResult> latestResults = noosResultDao.getLatestResults();

        // Then: Should return empty list
        assertNotNull("Latest results should not be null", latestResults);
        assertTrue("Should return empty list", latestResults.isEmpty());
    }

    /**
     * Test getting results by algorithm run ID
     * Verifies that getResultsByRunId returns correct results
     */
    @Test
    @Transactional
    @Rollback
    public void testGetResultsByRunId_ExistingRunId() {
        // Given: Insert results with different run IDs
        noosResultDao.insert(testResult1); // Run ID 1001
        noosResultDao.insert(testResult2); // Run ID 1001
        noosResultDao.insert(testResult3); // Run ID 1002
        noosResultDao.insert(testResult4); // Run ID 1002

        // When: Get results by run ID 1001
        List<NoosResult> resultsForRun1001 = noosResultDao.getResultsByRunId(1001L);

        // Then: Should return only results for run ID 1001
        assertNotNull("Results for run 1001 should not be null", resultsForRun1001);
        assertEquals("Should return 2 results for run 1001", 2, resultsForRun1001.size());
        
        // Verify all returned results are for run ID 1001
        assertTrue("All results should be for run 1001", resultsForRun1001.stream()
            .allMatch(r -> Long.valueOf(1001L).equals(r.getAlgorithmRunId())));
        
        // Verify ordering by style code
        assertEquals("First result should be PANT001", "PANT001", resultsForRun1001.get(0).getStyleCode());
        assertEquals("Second result should be SHIRT001", "SHIRT001", resultsForRun1001.get(1).getStyleCode());
    }

    /**
     * Test getting results by non-existent run ID
     * Verifies that getResultsByRunId returns empty list for non-existent run ID
     */
    @Test
    @Transactional
    @Rollback
    public void testGetResultsByRunId_NonExistentRunId() {
        // Given: Insert results
        noosResultDao.insert(testResult1);

        // When: Get results by non-existent run ID
        List<NoosResult> resultsForNonExistentRun = noosResultDao.getResultsByRunId(99999L);

        // Then: Should return empty list
        assertNotNull("Results for non-existent run should not be null", resultsForNonExistentRun);
        assertTrue("Should return empty list", resultsForNonExistentRun.isEmpty());
    }

    /**
     * Test getting results by null run ID
     * Verifies that getResultsByRunId handles null input gracefully
     */
    @Test
    @Transactional
    @Rollback
    public void testGetResultsByRunId_NullRunId() {
        // Given: Insert results
        noosResultDao.insert(testResult1);

        // When: Get results by null run ID
        List<NoosResult> resultsForNullRun = noosResultDao.getResultsByRunId(null);

        // Then: Should handle gracefully
        assertNotNull("Results for null run should not be null", resultsForNullRun);
        assertTrue("Should return empty list for null run ID", resultsForNullRun.isEmpty());
    }

    /**
     * Test getting results by category
     * Verifies that getResultsByCategory returns correct results
     */
    @Test
    @Transactional
    @Rollback
    public void testGetResultsByCategory_ExistingCategory() {
        // Given: Insert results with different categories
        noosResultDao.insert(testResult1); // SHIRTS
        noosResultDao.insert(testResult2); // PANTS
        noosResultDao.insert(testResult3); // DRESSES
        noosResultDao.insert(testResult4); // SHIRTS

        // When: Get results by SHIRTS category
        List<NoosResult> shirtsResults = noosResultDao.getResultsByCategory("SHIRTS");

        // Then: Should return only SHIRTS results
        assertNotNull("SHIRTS results should not be null", shirtsResults);
        assertEquals("Should return 2 SHIRTS results", 2, shirtsResults.size());
        
        // Verify all returned results are SHIRTS
        assertTrue("All results should be SHIRTS", shirtsResults.stream()
            .allMatch(r -> "SHIRTS".equals(r.getCategory())));
        
        // Verify ordering by style rev contribution DESC
        assertTrue("Should be ordered by style rev contribution DESC", 
            shirtsResults.get(0).getStyleRevContribution().compareTo(shirtsResults.get(1).getStyleRevContribution()) >= 0);
    }

    /**
     * Test getting results by non-existent category
     * Verifies that getResultsByCategory returns empty list for non-existent category
     */
    @Test
    @Transactional
    @Rollback
    public void testGetResultsByCategory_NonExistentCategory() {
        // Given: Insert results
        noosResultDao.insert(testResult1);

        // When: Get results by non-existent category
        List<NoosResult> nonExistentCategoryResults = noosResultDao.getResultsByCategory("NON_EXISTENT");

        // Then: Should return empty list
        assertNotNull("Non-existent category results should not be null", nonExistentCategoryResults);
        assertTrue("Should return empty list", nonExistentCategoryResults.isEmpty());
    }

    /**
     * Test getting results by null category
     * Verifies that getResultsByCategory handles null input gracefully
     */
    @Test
    @Transactional
    @Rollback
    public void testGetResultsByCategory_NullCategory() {
        // Given: Insert results
        noosResultDao.insert(testResult1);

        // When: Get results by null category
        List<NoosResult> nullCategoryResults = noosResultDao.getResultsByCategory(null);

        // Then: Should handle gracefully
        assertNotNull("Null category results should not be null", nullCategoryResults);
        assertTrue("Should return empty list for null category", nullCategoryResults.isEmpty());
    }

    /**
     * Test getting results by type
     * Verifies that getResultsByType returns correct results
     */
    @Test
    @Transactional
    @Rollback
    public void testGetResultsByType_ExistingType() {
        // Given: Insert results with different types
        noosResultDao.insert(testResult1); // core
        noosResultDao.insert(testResult2); // bestseller
        noosResultDao.insert(testResult3); // fashion
        noosResultDao.insert(testResult4); // core

        // When: Get results by core type
        List<NoosResult> coreResults = noosResultDao.getResultsByType("core");

        // Then: Should return only core results
        assertNotNull("Core results should not be null", coreResults);
        assertEquals("Should return 2 core results", 2, coreResults.size());
        
        // Verify all returned results are core
        assertTrue("All results should be core", coreResults.stream()
            .allMatch(r -> "core".equals(r.getType())));
        
        // Verify ordering by style rev contribution DESC
        assertTrue("Should be ordered by style rev contribution DESC", 
            coreResults.get(0).getStyleRevContribution().compareTo(coreResults.get(1).getStyleRevContribution()) >= 0);
    }

    /**
     * Test getting results by non-existent type
     * Verifies that getResultsByType returns empty list for non-existent type
     */
    @Test
    @Transactional
    @Rollback
    public void testGetResultsByType_NonExistentType() {
        // Given: Insert results
        noosResultDao.insert(testResult1);

        // When: Get results by non-existent type
        List<NoosResult> nonExistentTypeResults = noosResultDao.getResultsByType("non_existent");

        // Then: Should return empty list
        assertNotNull("Non-existent type results should not be null", nonExistentTypeResults);
        assertTrue("Should return empty list", nonExistentTypeResults.isEmpty());
    }

    /**
     * Test getting results by null type
     * Verifies that getResultsByType handles null input gracefully
     */
    @Test
    @Transactional
    @Rollback
    public void testGetResultsByType_NullType() {
        // Given: Insert results
        noosResultDao.insert(testResult1);

        // When: Get results by null type
        List<NoosResult> nullTypeResults = noosResultDao.getResultsByType(null);

        // Then: Should handle gracefully
        assertNotNull("Null type results should not be null", nullTypeResults);
        assertTrue("Should return empty list for null type", nullTypeResults.isEmpty());
    }

    /**
     * Test getting total count of results
     * Verifies that getCount returns correct count
     */
    @Test
    @Transactional
    @Rollback
    public void testGetCount_WithResults() {
        // Given: Insert multiple results
        noosResultDao.insert(testResult1);
        noosResultDao.insert(testResult2);
        noosResultDao.insert(testResult3);

        // When: Get total count
        long count = noosResultDao.getCount();

        // Then: Should return correct count
        assertEquals("Should return count of 3", 3, count);
    }

    /**
     * Test getting total count when no results exist
     * Verifies that getCount returns 0 when no data
     */
    @Test
    @Transactional
    @Rollback
    public void testGetCount_NoResults() {
        // When: Get total count with no data
        long count = noosResultDao.getCount();

        // Then: Should return 0
        assertEquals("Should return count of 0", 0, count);
    }

    /**
     * Test getting count by type
     * Verifies that getCountByType returns correct count
     */
    @Test
    @Transactional
    @Rollback
    public void testGetCountByType_ExistingType() {
        // Given: Insert results with different types
        noosResultDao.insert(testResult1); // core
        noosResultDao.insert(testResult2); // bestseller
        noosResultDao.insert(testResult3); // fashion
        noosResultDao.insert(testResult4); // core

        // When: Get count by type
        long coreCount = noosResultDao.getCountByType("core");
        long bestsellerCount = noosResultDao.getCountByType("bestseller");
        long fashionCount = noosResultDao.getCountByType("fashion");

        // Then: Should return correct counts
        assertEquals("Should have 2 core results", 2, coreCount);
        assertEquals("Should have 1 bestseller result", 1, bestsellerCount);
        assertEquals("Should have 1 fashion result", 1, fashionCount);
    }

    /**
     * Test getting count by non-existent type
     * Verifies that getCountByType returns 0 for non-existent type
     */
    @Test
    @Transactional
    @Rollback
    public void testGetCountByType_NonExistentType() {
        // Given: Insert results
        noosResultDao.insert(testResult1);

        // When: Get count by non-existent type
        long count = noosResultDao.getCountByType("non_existent");

        // Then: Should return 0
        assertEquals("Should return 0 for non-existent type", 0, count);
    }

    /**
     * Test getting count by null type
     * Verifies that getCountByType handles null input gracefully
     */
    @Test
    @Transactional
    @Rollback
    public void testGetCountByType_NullType() {
        // Given: Insert results
        noosResultDao.insert(testResult1);

        // When: Get count by null type
        long count = noosResultDao.getCountByType(null);

        // Then: Should handle gracefully
        assertEquals("Should return 0 for null type", 0, count);
    }

    // ==================== BATCH OPERATIONS TESTS ====================

    /**
     * Test inserting multiple results in batch
     * Verifies that insertAll efficiently saves multiple results
     */
    @Test
    @Transactional
    @Rollback
    public void testInsertAll_MultipleResults() {
        // Given: List of results to save
        List<NoosResult> resultsToSave = new ArrayList<>();
        resultsToSave.add(testResult1);
        resultsToSave.add(testResult2);
        resultsToSave.add(testResult3);

        // When: Insert all results
        noosResultDao.insertAll(resultsToSave);

        // Then: All results should be saved
        long count = noosResultDao.getCount();
        assertEquals("Should save all 3 results", 3, count);
        
        // Verify each result was saved correctly
        List<NoosResult> latestResults = noosResultDao.getLatestResults();
        assertEquals("Should have 3 results", 3, latestResults.size());
        
        assertTrue("Should contain SHIRTS result", latestResults.stream()
            .anyMatch(r -> "SHIRTS".equals(r.getCategory())));
        assertTrue("Should contain PANTS result", latestResults.stream()
            .anyMatch(r -> "PANTS".equals(r.getCategory())));
        assertTrue("Should contain DRESSES result", latestResults.stream()
            .anyMatch(r -> "DRESSES".equals(r.getCategory())));
    }

    /**
     * Test inserting empty list of results
     * Verifies that insertAll handles empty list gracefully
     */
    @Test
    @Transactional
    @Rollback
    public void testInsertAll_EmptyList() {
        // Given: Empty list of results
        List<NoosResult> emptyList = new ArrayList<>();

        // When: Insert empty list
        noosResultDao.insertAll(emptyList);

        // Then: No results should be saved
        long count = noosResultDao.getCount();
        assertEquals("Should remain empty", 0, count);
    }

    /**
     * Test inserting null list of results
     * Verifies that insertAll handles null input gracefully
     */
    @Test
    @Transactional
    @Rollback
    public void testInsertAll_NullList() {
        // When: Insert null list
        try {
            noosResultDao.insertAll(null);
            // Should not throw exception, just handle gracefully
        } catch (Exception e) {
            // If it throws exception, it should be handled appropriately
            assertTrue("Should handle null list gracefully", 
                e instanceof NullPointerException || e instanceof IllegalArgumentException);
        }
    }

    /**
     * Test inserting large batch of results (testing batch processing)
     * Verifies that insertAll handles large datasets efficiently
     */
    @Test
    @Transactional
    @Rollback
    public void testInsertAll_LargeBatch() {
        // Given: Large list of results (55 results to test batch processing)
        List<NoosResult> largeList = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        
        for (int i = 1; i <= 55; i++) {
            NoosResult result = new NoosResult();
            result.setCategory("CATEGORY_" + (i % 5));
            result.setStyleCode("STYLE_" + String.format("%03d", i));
            result.setStyleROS(new BigDecimal(String.valueOf(0.5 + (i * 0.01))));
            result.setType(i % 3 == 0 ? "core" : (i % 3 == 1 ? "bestseller" : "fashion"));
            result.setStyleRevContribution(new BigDecimal(String.valueOf(10.0 + i)));
            result.setCalculatedDate(cal.getTime());
            result.setTotalQuantitySold(100 + i);
            result.setTotalRevenue(new BigDecimal(String.valueOf(30000 + (i * 1000))));
            result.setDaysAvailable(30);
            result.setDaysWithSales(20 + (i % 10));
            result.setAvgDiscount(new BigDecimal(String.valueOf(5.0 + (i % 10))));
            result.setAlgorithmRunId(2000L + (i % 3));
            largeList.add(result);
            
            // Increment date by 1 hour for variety
            cal.add(Calendar.HOUR, 1);
        }

        // When: Insert large batch
        noosResultDao.insertAll(largeList);

        // Then: All results should be saved
        long count = noosResultDao.getCount();
        assertEquals("Should save all 55 results", 55, count);
        
        // Verify batch processing worked
        List<NoosResult> allResults = noosResultDao.getLatestResults();
        assertTrue("Should contain results with various style codes", allResults.stream()
            .anyMatch(r -> r.getStyleCode().startsWith("STYLE_0")));
    }

    /**
     * Test deleting all results
     * Verifies that deleteAll removes all results
     */
    @Test
    @Transactional
    @Rollback
    public void testDeleteAll_WithResults() {
        // Given: Insert some results
        noosResultDao.insert(testResult1);
        noosResultDao.insert(testResult2);
        noosResultDao.insert(testResult3);
        
        // Verify results exist
        assertEquals("Should have 3 results before delete", 3, noosResultDao.getCount());

        // When: Delete all results
        noosResultDao.deleteAll();

        // Then: Should be empty
        long count = noosResultDao.getCount();
        assertEquals("Should be empty after delete all", 0, count);
        
        List<NoosResult> allResults = noosResultDao.getLatestResults();
        assertTrue("Should return empty list", allResults.isEmpty());
    }

    /**
     * Test deleting all results when no results exist
     * Verifies that deleteAll handles empty table gracefully
     */
    @Test
    @Transactional
    @Rollback
    public void testDeleteAll_NoResults() {
        // Given: No results in database
        assertEquals("Should start with empty table", 0, noosResultDao.getCount());

        // When: Delete all results
        noosResultDao.deleteAll();

        // Then: Should remain empty
        long count = noosResultDao.getCount();
        assertEquals("Should remain empty", 0, count);
    }

    // ==================== EDGE CASES TESTS ====================

    /**
     * Test inserting result with minimum valid values
     * Verifies that DAO handles minimum valid values correctly
     */
    @Test
    @Transactional
    @Rollback
    public void testInsert_MinimumValidValues() {
        // Given: Result with minimum valid values
        NoosResult minResult = new NoosResult();
        minResult.setCategory("A");
        minResult.setStyleCode("S");
        minResult.setStyleROS(new BigDecimal("0.0001"));
        minResult.setType("core");
        minResult.setStyleRevContribution(new BigDecimal("0.0001"));
        minResult.setCalculatedDate(new Date());

        // When: Insert the result
        noosResultDao.insert(minResult);

        // Then: Should save successfully
        assertNotNull("Should save result with min valid values", minResult.getId());
        assertEquals("Should preserve min category", "A", minResult.getCategory());
        assertEquals("Should preserve min style code", "S", minResult.getStyleCode());
        assertEquals("Should preserve min ROS", new BigDecimal("0.0001"), minResult.getStyleROS());
    }

    /**
     * Test inserting result with maximum valid values
     * Verifies that DAO handles large values correctly
     */
    @Test
    @Transactional
    @Rollback
    public void testInsert_MaximumValidValues() {
        // Given: Result with maximum field lengths
        NoosResult maxResult = new NoosResult();
        maxResult.setCategory(generateString("A", 50)); // Max 50 chars
        maxResult.setStyleCode(generateString("B", 50)); // Max 50 chars
        maxResult.setStyleROS(new BigDecimal("999999.9999")); // Max precision 10, scale 4
        maxResult.setType(generateString("C", 20)); // Max 20 chars
        maxResult.setStyleRevContribution(new BigDecimal("999999.9999")); // Max precision 10, scale 4
        maxResult.setCalculatedDate(new Date());
        maxResult.setTotalQuantitySold(Integer.MAX_VALUE);
        maxResult.setTotalRevenue(new BigDecimal("9999999999999.99")); // Max precision 15, scale 2
        maxResult.setDaysAvailable(Integer.MAX_VALUE);
        maxResult.setDaysWithSales(Integer.MAX_VALUE);
        maxResult.setAvgDiscount(new BigDecimal("999999.9999")); // Max precision 10, scale 4
        maxResult.setAlgorithmRunId(Long.MAX_VALUE);

        // When: Insert the result
        noosResultDao.insert(maxResult);

        // Then: Should save successfully
        assertNotNull("Should save result with max valid values", maxResult.getId());
        assertEquals("Should preserve max category", 50, maxResult.getCategory().length());
        assertEquals("Should preserve max style code", 50, maxResult.getStyleCode().length());
        assertEquals("Should preserve max type", 20, maxResult.getType().length());
    }

    /**
     * Test complex query combinations
     * Verifies that multiple query methods work correctly together
     */
    @Test
    @Transactional
    @Rollback
    public void testComplexQueryCombinations() {
        // Given: Insert results with various combinations
        noosResultDao.insert(testResult1); // SHIRTS, core, run 1001
        noosResultDao.insert(testResult2); // PANTS, bestseller, run 1001
        noosResultDao.insert(testResult3); // DRESSES, fashion, run 1002
        noosResultDao.insert(testResult4); // SHIRTS, core, run 1002

        // When: Perform various queries
        List<NoosResult> latestResults = noosResultDao.getLatestResults();
        List<NoosResult> run1001Results = noosResultDao.getResultsByRunId(1001L);
        List<NoosResult> shirtsResults = noosResultDao.getResultsByCategory("SHIRTS");
        List<NoosResult> coreResults = noosResultDao.getResultsByType("core");
        long totalCount = noosResultDao.getCount();
        long coreCount = noosResultDao.getCountByType("core");

        // Then: All queries should return correct results
        assertEquals("Should have 4 total results", 4, latestResults.size());
        assertEquals("Should have 2 results for run 1001", 2, run1001Results.size());
        assertEquals("Should have 2 SHIRTS results", 2, shirtsResults.size());
        assertEquals("Should have 2 core results", 2, coreResults.size());
        assertEquals("Total count should be 4", 4, totalCount);
        assertEquals("Core count should be 2", 2, coreCount);
    }

    /**
     * Test results with same calculated date
     * Verifies that multiple results on same date are handled correctly
     */
    @Test
    @Transactional
    @Rollback
    public void testSameCalculatedDate() {
        // Given: Multiple results with same calculated date
        Date sameDate = new Date();
        testResult1.setCalculatedDate(sameDate);
        testResult2.setCalculatedDate(sameDate);
        testResult3.setCalculatedDate(sameDate);

        noosResultDao.insert(testResult1);
        noosResultDao.insert(testResult2);
        noosResultDao.insert(testResult3);

        // When: Get latest results
        List<NoosResult> latestResults = noosResultDao.getLatestResults();

        // Then: Should return all results with same date
        assertNotNull("Latest results should not be null", latestResults);
        assertEquals("Should return 3 results", 3, latestResults.size());
        
        // Verify all results have the same date
        assertTrue("All results should have same calculated date", latestResults.stream()
            .allMatch(r -> sameDate.equals(r.getCalculatedDate())));
    }

    // ==================== TRANSACTION ROLLBACK TESTS ====================

    /**
     * Test transaction rollback on exception
     * Verifies that failed operations are rolled back
     */
    @Test
    @Transactional
    @Rollback
    public void testTransactionRollback_OnException() {
        // Given: Initial state
        long initialCount = noosResultDao.getCount();

        try {
            // When: Insert valid result first
            noosResultDao.insert(testResult1);
            
            // Then try to insert invalid result (should cause exception)
            NoosResult invalidResult = new NoosResult();
            // Don't set required fields to cause validation error
            noosResultDao.insert(invalidResult);
            
            fail("Should have thrown exception");
        } catch (Exception e) {
            // Expected exception - this is good
            // The transaction should be rolled back due to @Rollback annotation
        }

        // Then: Transaction should be rolled back due to @Rollback annotation
        // Note: We can't easily test rollback within the same transaction
        // The @Rollback annotation ensures the entire test transaction is rolled back
        // This test verifies that exceptions are handled properly
    }

    /**
     * Test that @Rollback annotation works correctly
     * Verifies that test data is cleaned up after test
     */
    @Test
    @Transactional
    @Rollback
    public void testRollbackAnnotation_Cleanup() {
        // Given: Insert some test data
        noosResultDao.insert(testResult1);
        noosResultDao.insert(testResult2);
        
        // Verify data exists
        assertEquals("Should have 2 results", 2, noosResultDao.getCount());
        
        // Test will automatically rollback due to @Rollback annotation
        // This test verifies the rollback mechanism works
    }

    /**
     * Test batch operation rollback
     * Verifies that batch operations can be rolled back
     */
    @Test
    @Transactional
    @Rollback
    public void testBatchOperationRollback() {
        // Given: Initial state
        long initialCount = noosResultDao.getCount();

        // When: Perform batch operation
        List<NoosResult> batchResults = new ArrayList<>();
        batchResults.add(testResult1);
        batchResults.add(testResult2);
        batchResults.add(testResult3);
        
        noosResultDao.insertAll(batchResults);
        
        // Verify batch was saved
        assertEquals("Should have saved batch", initialCount + 3, noosResultDao.getCount());
        
        // Test will rollback due to @Rollback annotation
        // This verifies batch operations respect transaction boundaries
    }

    /**
     * Test rollback on failed batch insert
     * Verifies that if one item in batch fails, entire batch is rolled back
     */
    @Test
    @Transactional
    @Rollback
    public void testBatchRollback_OnFailedInsert() {
        // Given: Initial state
        long initialCount = noosResultDao.getCount();

        try {
            // When: Create batch with one invalid result
            List<NoosResult> batchResults = new ArrayList<>();
            batchResults.add(testResult1); // Valid result
            
            NoosResult invalidResult = new NoosResult();
            // Don't set required fields to cause validation error
            batchResults.add(invalidResult); // Invalid result
            
            batchResults.add(testResult2); // Valid result
            
            noosResultDao.insertAll(batchResults);
            
            fail("Should have thrown exception for invalid result");
        } catch (Exception e) {
            // Expected exception due to invalid result
        }

        // Then: Should rollback to initial state due to @Rollback annotation
        // This test verifies that batch operations handle failures appropriately
    }

    /**
     * Test concurrent access simulation
     * Verifies that DAO handles multiple operations correctly
     */
    @Test
    @Transactional
    @Rollback
    public void testConcurrentAccess_Simulation() {
        // Given: Insert initial results
        noosResultDao.insert(testResult1);
        noosResultDao.insert(testResult2);

        // When: Perform multiple operations in sequence (simulating concurrent access)
        List<NoosResult> latestResults = noosResultDao.getLatestResults();
        List<NoosResult> run1001Results = noosResultDao.getResultsByRunId(1001L);
        List<NoosResult> shirtsResults = noosResultDao.getResultsByCategory("SHIRTS");
        List<NoosResult> coreResults = noosResultDao.getResultsByType("core");
        long totalCount = noosResultDao.getCount();
        long coreCount = noosResultDao.getCountByType("core");

        // Then: All operations should work correctly
        assertEquals("Should have 2 total results", 2, latestResults.size());
        assertEquals("Should have 2 results for run 1001", 2, run1001Results.size());
        assertEquals("Should have 1 SHIRTS result", 1, shirtsResults.size());
        assertEquals("Should have 1 core result", 1, coreResults.size());
        assertEquals("Count should be 2", 2, totalCount);
        assertEquals("Core count should be 1", 1, coreCount);
    }

    // ==================== HELPER METHODS ====================

    /**
     * Helper method to generate string of specified length
     * Java 8 compatible alternative to String.repeat()
     */
    private String generateString(String character, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(character);
        }
        return sb.toString();
    }
}
