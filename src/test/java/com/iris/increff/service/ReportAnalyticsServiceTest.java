package com.iris.increff.service;

import com.iris.increff.AbstractUnitTest;
import com.iris.increff.dao.*;
import com.iris.increff.model.*;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Comprehensive test suite for ReportAnalyticsService
 * 
 * Tests all critical functionality including:
 * - NOOS Analytics Report generation with real and sample data
 * - System Health Report generation with task statistics
 * - Sample data generation for empty datasets
 * - Status determination logic for different scenarios
 * - Error handling and fallback mechanisms
 * - Integration with NoosAlgorithmService and TaskDao
 * 
 * This service is critical for PRD compliance: "Report generation and analytics"
 * and "System monitoring and performance tracking"
 * 
 * Target: 90-95% method and line coverage for ReportAnalyticsService
 * 
 * @author Anshuk Kawarry
 * @version 1.0
 * @since 2025-01-01
 */
public class ReportAnalyticsServiceTest extends AbstractUnitTest {

    @Autowired
    private ReportAnalyticsService reportAnalyticsService;

    @Autowired
    private NoosAlgorithmService noosAlgorithmService;

    @Autowired
    private DataClearingService dataClearingService;

    @Autowired
    private StyleService styleService;

    @Autowired
    private StoreService storeService;

    @Autowired
    private SkuService skuService;

    @Autowired
    private SalesService salesService;

    @Autowired
    private TaskDao taskDao;

    @Autowired
    private NoosResultDao noosResultDao;

    @Autowired
    private SalesDao salesDao;

    /**
     * Setup test data before each test method
     * Ensures clean state for consistent testing
     */
    @Before
    public void setUp() {
        dataClearingService.clearAllData();
    }

    // ==================== NOOS ANALYTICS REPORT TESTS ====================

    /**
     * Test NOOS analytics report with no data
     * Verifies that service generates sample data when no real executions exist
     */
    @Test
    @Transactional
    @Rollback
    public void testGenerateNoosAnalyticsReport_NoData() {
        // When: Generate NOOS analytics report with no data
        List<Report1Data> report = reportAnalyticsService.generateNoosAnalyticsReport();

        // Then: Should return sample data
        assertNotNull("Report should not be null", report);
        assertFalse("Report should not be empty", report.isEmpty());
        assertEquals("Should return 5 sample entries", 5, report.size());

        // Verify sample data structure
        Report1Data firstEntry = report.get(0);
        assertNotNull("Report entry should not be null", firstEntry);
        assertNotNull("Execution date should not be null", firstEntry.getExecutionDate());
        assertNotNull("Algorithm label should not be null", firstEntry.getAlgorithmLabel());
        assertTrue("Algorithm label should contain 'Sample'", firstEntry.getAlgorithmLabel().contains("Sample"));
        assertEquals("Status should be COMPLETED", "COMPLETED", firstEntry.getExecutionStatus());
        assertTrue("Total styles should be > 0", firstEntry.getTotalStylesProcessed() > 0);
        assertTrue("Core count should be > 0", firstEntry.getCoreStyles() > 0);
        assertTrue("Bestseller count should be > 0", firstEntry.getBestsellerStyles() > 0);
        assertTrue("Fashion count should be > 0", firstEntry.getFashionStyles() > 0);
        assertTrue("Execution time should be > 0", firstEntry.getExecutionTimeMinutes() > 0);
        assertNotNull("Parameters should not be null", firstEntry.getParameters());
    }

    /**
     * Test NOOS analytics report with real data
     * Verifies that service processes real NOOS results correctly
     */
    @Test
    @Transactional
    @Rollback
    public void testGenerateNoosAnalyticsReport_WithRealData() throws Exception {
        // Given: Create test data and run NOOS algorithm
        createSampleDataForNoos();
        createSampleNoosResults();

        // When: Generate NOOS analytics report
        List<Report1Data> report = reportAnalyticsService.generateNoosAnalyticsReport();

        // Then: Should return real data analysis
        assertNotNull("Report should not be null", report);
        assertFalse("Report should not be empty", report.isEmpty());

        // Verify real data characteristics
        Report1Data firstEntry = report.get(0);
        assertNotNull("Report entry should not be null", firstEntry);
        assertTrue("Algorithm label should contain 'NOOS Analysis'", firstEntry.getAlgorithmLabel().contains("NOOS Analysis"));
        assertEquals("Status should be COMPLETED", "COMPLETED", firstEntry.getExecutionStatus());
        assertTrue("Should have processed styles", firstEntry.getTotalStylesProcessed() > 0);
        assertNotEquals("Should not be sample parameters", "Sample parameters", firstEntry.getParameters());
    }

    /**
     * Test NOOS analytics report with multiple execution dates
     * Verifies grouping and aggregation of results by date
     */
    @Test
    @Transactional
    @Rollback
    public void testGenerateNoosAnalyticsReport_MultipleExecutions() throws Exception {
        // Given: Create NOOS results with different dates
        createMultipleDateNoosResults();

        // When: Generate NOOS analytics report
        List<Report1Data> report = reportAnalyticsService.generateNoosAnalyticsReport();

        // Then: Should group results by execution date
        assertNotNull("Report should not be null", report);
        assertFalse("Report should not be empty", report.isEmpty());

        // Verify grouping worked correctly
        for (Report1Data entry : report) {
            assertNotNull("Execution date should not be null", entry.getExecutionDate());
            assertTrue("Should have total styles", entry.getTotalStylesProcessed() > 0);
            // Core + Bestseller + Fashion should equal total (for our test data)
            int totalClassified = entry.getCoreStyles() + entry.getBestsellerStyles() + entry.getFashionStyles();
            assertEquals("Classification counts should sum to total", entry.getTotalStylesProcessed(), (Integer) totalClassified);
        }
    }

    // ==================== SYSTEM HEALTH REPORT TESTS ====================

    /**
     * Test system health report generation
     * Verifies task statistics and system status calculation
     */
    @Test
    @Transactional
    @Rollback
    public void testGenerateSystemHealthReport() {
        // When: Generate system health report
        List<Report2Data> report = reportAnalyticsService.generateSystemHealthReport();

        // Then: Should return system health data
        assertNotNull("Report should not be null", report);
        assertFalse("Report should not be empty", report.isEmpty());

        // Should include system overview as first entry
        Report2Data systemOverview = report.get(0);
        assertEquals("First entry should be system overview", "SYSTEM_OVERVIEW", systemOverview.getTaskType());
        assertNotNull("System status should not be null", systemOverview.getSystemStatus());

        // Should include different task types
        boolean hasUploadSales = report.stream().anyMatch(r -> "UPLOAD_SALES".equals(r.getTaskType()));
        boolean hasUploadStyles = report.stream().anyMatch(r -> "UPLOAD_STYLES".equals(r.getTaskType()));
        assertTrue("Should include UPLOAD_SALES task type", hasUploadSales);
        assertTrue("Should include UPLOAD_STYLES task type", hasUploadStyles);

        // Verify report structure
        for (Report2Data entry : report) {
            assertNotNull("Task type should not be null", entry.getTaskType());
            assertNotNull("Report date should not be null", entry.getDate());
            assertTrue("Total tasks should be >= 0", entry.getTotalTasks() >= 0);
            assertTrue("Successful tasks should be >= 0", entry.getSuccessfulTasks() >= 0);
            assertTrue("Failed tasks should be >= 0", entry.getFailedTasks() >= 0);
            assertTrue("Success rate should be >= 0", entry.getSuccessRate() >= 0.0);
            assertTrue("Success rate should be <= 100", entry.getSuccessRate() <= 100.0);
            assertTrue("Execution time should be >= 0", entry.getAverageExecutionTime() >= 0.0);
            assertNotNull("System status should not be null", entry.getSystemStatus());
        }
    }

    /**
     * Test system health report with task data
     * Verifies integration with TaskDao for real statistics
     */
    @Test
    @Transactional
    @Rollback
    public void testGenerateSystemHealthReport_WithTaskData() throws Exception {
        // Given: Create some task data
        createSampleTasks();

        // When: Generate system health report
        List<Report2Data> report = reportAnalyticsService.generateSystemHealthReport();

        // Then: Should include task statistics
        assertNotNull("Report should not be null", report);
        assertFalse("Report should not be empty", report.isEmpty());

        // Verify system overview includes task information
        Report2Data systemOverview = report.get(0);
        assertEquals("System overview should be first", "SYSTEM_OVERVIEW", systemOverview.getTaskType());
        
        // System status should reflect data availability
        String systemStatus = systemOverview.getSystemStatus();
        assertTrue("System status should be valid", 
                  "HEALTHY".equals(systemStatus) || "NEEDS_DATA".equals(systemStatus) ||
                  "EXCELLENT".equals(systemStatus) || "GOOD".equals(systemStatus) ||
                  "WARNING".equals(systemStatus) || "CRITICAL".equals(systemStatus) ||
                  "NO_ACTIVITY".equals(systemStatus));
    }

    // ==================== SAMPLE DATA GENERATION TESTS ====================

    /**
     * Test sample NOOS data generation
     * Verifies fallback sample data is properly structured
     */
    @Test
    @Transactional
    @Rollback
    public void testSampleDataGeneration() {
        // When: Generate report with no real data (triggers sample data generation)
        List<Report1Data> noosReport = reportAnalyticsService.generateNoosAnalyticsReport();
        List<Report2Data> healthReport = reportAnalyticsService.generateSystemHealthReport();

        // Then: Should generate valid sample data
        assertNotNull("NOOS report should not be null", noosReport);
        assertNotNull("Health report should not be null", healthReport);
        
        assertFalse("NOOS report should not be empty", noosReport.isEmpty());
        assertFalse("Health report should not be empty", healthReport.isEmpty());

        // Verify sample NOOS data quality
        for (Report1Data entry : noosReport) {
            assertTrue("Sample data should have reasonable total styles", entry.getTotalStylesProcessed() >= 1000);
            assertTrue("Sample data should have reasonable core count", entry.getCoreStyles() >= 200);
            assertTrue("Sample data should have reasonable bestseller count", entry.getBestsellerStyles() >= 150);
            assertTrue("Sample data should have reasonable fashion count", entry.getFashionStyles() >= 400);
            assertTrue("Sample execution time should be reasonable", entry.getExecutionTimeMinutes() >= 1.0);
        }

        // Verify sample health data quality
        for (Report2Data entry : healthReport) {
            assertTrue("Sample total tasks should be reasonable", entry.getTotalTasks() >= 0);
            assertTrue("Sample success rate should be valid", entry.getSuccessRate() >= 0.0 && entry.getSuccessRate() <= 100.0);
            assertTrue("Sample execution time should be reasonable", entry.getAverageExecutionTime() >= 0.0);
            assertNotNull("Sample system status should not be null", entry.getSystemStatus());
        }
    }

    // ==================== STATUS DETERMINATION TESTS ====================

    /**
     * Test system status determination logic
     * Verifies correct status calculation for different scenarios
     */
    @Test
    @Transactional
    @Rollback
    public void testSystemStatusDetermination() {
        // Test through system health report generation
        List<Report2Data> report = reportAnalyticsService.generateSystemHealthReport();
        
        assertNotNull("Report should not be null", report);
        assertFalse("Report should not be empty", report.isEmpty());

        // Verify status determination logic through generated data
        for (Report2Data entry : report) {
            String status = entry.getSystemStatus();
            double successRate = entry.getSuccessRate();
            int totalTasks = entry.getTotalTasks();

            // Verify status logic consistency
            if (totalTasks == 0) {
                assertTrue("No activity should result in appropriate status", 
                          "NO_ACTIVITY".equals(status) || "NEEDS_DATA".equals(status) || "HEALTHY".equals(status));
            } else if (successRate >= 90.0) {
                assertTrue("High success rate should result in good status", 
                          "EXCELLENT".equals(status) || "HEALTHY".equals(status) || 
                          "GOOD".equals(status));
            }
            // Note: Other status conditions are harder to test without complex setup
        }
    }

    // ==================== ERROR HANDLING TESTS ====================

    /**
     * Test error handling and fallback mechanisms
     * Verifies service gracefully handles errors and returns sample data
     */
    @Test
    @Transactional
    @Rollback
    public void testErrorHandlingAndFallback() {
        // Note: It's difficult to simulate database errors in this test environment
        // The error handling is primarily tested through the normal flow
        // In case of errors, the service returns sample data

        // When: Generate reports (should work normally)
        List<Report1Data> noosReport = reportAnalyticsService.generateNoosAnalyticsReport();
        List<Report2Data> healthReport = reportAnalyticsService.generateSystemHealthReport();

        // Then: Should return valid reports (sample data in case of no real data)
        assertNotNull("NOOS report should not be null", noosReport);
        assertNotNull("Health report should not be null", healthReport);
        assertFalse("NOOS report should not be empty", noosReport.isEmpty());
        assertFalse("Health report should not be empty", healthReport.isEmpty());

        // Verify error handling doesn't break report structure
        for (Report1Data entry : noosReport) {
            assertNotNull("Entry should not be null", entry);
            assertNotNull("Execution date should not be null", entry.getExecutionDate());
            assertNotNull("Algorithm label should not be null", entry.getAlgorithmLabel());
            assertNotNull("Status should not be null", entry.getExecutionStatus());
        }

        for (Report2Data entry : healthReport) {
            assertNotNull("Entry should not be null", entry);
            assertNotNull("Task type should not be null", entry.getTaskType());
            assertNotNull("Report date should not be null", entry.getDate());
            assertNotNull("System status should not be null", entry.getSystemStatus());
        }
    }

    // ==================== INTEGRATION TESTS ====================

    /**
     * Test integration with NoosAlgorithmService
     * Verifies service correctly integrates with NOOS algorithm results
     */
    @Test
    @Transactional
    @Rollback
    public void testIntegrationWithNoosAlgorithmService() throws Exception {
        // Given: Create data and NOOS results
        createSampleDataForNoos();
        createSampleNoosResults();

        // When: Generate NOOS analytics report
        List<Report1Data> report = reportAnalyticsService.generateNoosAnalyticsReport();

        // Then: Should integrate with NoosAlgorithmService correctly
        assertNotNull("Report should not be null", report);
        assertFalse("Report should not be empty", report.isEmpty());

        // Verify integration worked by checking for real data characteristics
        boolean hasRealData = report.stream().anyMatch(entry -> 
            entry.getAlgorithmLabel().contains("NOOS Analysis") && 
            !"Sample parameters".equals(entry.getParameters()));

        assertTrue("Should have integrated real NOOS data", hasRealData);
    }

    /**
     * Test report consistency across multiple calls
     * Verifies reports remain consistent when called multiple times
     */
    @Test
    @Transactional
    @Rollback
    public void testReportConsistency() throws Exception {
        // Given: Create sample data
        createSampleDataForNoos();
        createSampleNoosResults();

        // When: Generate reports multiple times
        List<Report1Data> noosReport1 = reportAnalyticsService.generateNoosAnalyticsReport();
        List<Report1Data> noosReport2 = reportAnalyticsService.generateNoosAnalyticsReport();
        
        List<Report2Data> healthReport1 = reportAnalyticsService.generateSystemHealthReport();
        List<Report2Data> healthReport2 = reportAnalyticsService.generateSystemHealthReport();

        // Then: Should return consistent results
        assertNotNull("First NOOS report should not be null", noosReport1);
        assertNotNull("Second NOOS report should not be null", noosReport2);
        assertNotNull("First health report should not be null", healthReport1);
        assertNotNull("Second health report should not be null", healthReport2);

        assertEquals("NOOS report size should be consistent", noosReport1.size(), noosReport2.size());
        assertEquals("Health report size should be consistent", healthReport1.size(), healthReport2.size());

        // Verify data consistency (sample data should be similar structure)
        if (!noosReport1.isEmpty() && !noosReport2.isEmpty()) {
            Report1Data entry1 = noosReport1.get(0);
            Report1Data entry2 = noosReport2.get(0);
            assertEquals("Status should be consistent", entry1.getExecutionStatus(), entry2.getExecutionStatus());
        }
    }

    // ==================== HELPER METHODS ====================

    /**
     * Create sample data needed for NOOS algorithm testing
     */
    private void createSampleDataForNoos() throws Exception {
        // Create styles
        String stylesData = "style\tbrand\tcategory\tsub_category\tmrp\tgender\n" +
                           "SHIRT001\tNIKE\tSHIRTS\tCASUAL\t100.50\tM\n" +
                           "PANT001\tADIDAS\tPANTS\tFORMAL\t150.75\tF\n" +
                           "DRESS001\tZARA\tDRESSES\tPARTY\t200.00\tF";
        styleService.processAndSaveStyles(createTsvDataFromString(stylesData));

        // Create stores
        String storesData = "branch\tcity\n" +
                           "MUMBAI_CENTRAL\tMUMBAI\n" +
                           "DELHI_CP\tDELHI";
        storeService.processAndSaveStores(createTsvDataFromString(storesData));

        // Create SKUs
        String skusData = "sku\tstyle\tsize\n" +
                         "SKU001\tSHIRT001\tM\n" +
                         "SKU002\tPANT001\tL\n" +
                         "SKU003\tDRESS001\tM";
        skuService.processAndSaveSKUs(createTsvDataFromString(skusData));

        // Create sales
        String salesData = "day\tsku\tchannel\tquantity\tdiscount\trevenue\n" +
                          "2024-01-15\tSKU001\tMUMBAI_CENTRAL\t5\t10.00\t450.00\n" +
                          "2024-01-16\tSKU002\tDELHI_CP\t3\t5.50\t280.50\n" +
                          "2024-01-17\tSKU003\tMUMBAI_CENTRAL\t2\t15.00\t340.00";
        salesService.processAndSaveSales(createTsvDataFromString(salesData));
    }

    /**
     * Create sample NOOS results for testing
     */
    private void createSampleNoosResults() {
        Date now = new Date();
        
        // Create NOOS results with different classifications
        NoosResult result1 = new NoosResult();
        result1.setCategory("SHIRTS");
        result1.setStyleCode("SHIRT001");
        result1.setStyleROS(new java.math.BigDecimal("1.5"));
        result1.setType("core");
        result1.setStyleRevContribution(new java.math.BigDecimal("25.5"));
        result1.setTotalQuantitySold(100);
        result1.setTotalRevenue(new java.math.BigDecimal("5000.0"));
        result1.setDaysAvailable(30);
        result1.setDaysWithSales(25);
        result1.setAvgDiscount(new java.math.BigDecimal("8.5"));
        result1.setCalculatedDate(now);
        noosResultDao.insert(result1);

        NoosResult result2 = new NoosResult();
        result2.setCategory("PANTS");
        result2.setStyleCode("PANT001");
        result2.setStyleROS(new java.math.BigDecimal("2.1"));
        result2.setType("bestseller");
        result2.setStyleRevContribution(new java.math.BigDecimal("35.2"));
        result2.setTotalQuantitySold(150);
        result2.setTotalRevenue(new java.math.BigDecimal("7500.0"));
        result2.setDaysAvailable(30);
        result2.setDaysWithSales(28);
        result2.setAvgDiscount(new java.math.BigDecimal("5.0"));
        result2.setCalculatedDate(now);
        noosResultDao.insert(result2);

        NoosResult result3 = new NoosResult();
        result3.setCategory("DRESSES");
        result3.setStyleCode("DRESS001");
        result3.setStyleROS(new java.math.BigDecimal("0.8"));
        result3.setType("fashion");
        result3.setStyleRevContribution(new java.math.BigDecimal("15.3"));
        result3.setTotalQuantitySold(50);
        result3.setTotalRevenue(new java.math.BigDecimal("3000.0"));
        result3.setDaysAvailable(30);
        result3.setDaysWithSales(15);
        result3.setAvgDiscount(new java.math.BigDecimal("20.0"));
        result3.setCalculatedDate(now);
        noosResultDao.insert(result3);
    }

    /**
     * Create NOOS results with multiple execution dates
     */
    private void createMultipleDateNoosResults() {
        Date today = new Date();
        Date yesterday = new Date(today.getTime() - 24 * 60 * 60 * 1000);
        
        // Results for today
        NoosResult result1 = new NoosResult();
        result1.setCategory("SHIRTS");
        result1.setStyleCode("SHIRT001");
        result1.setStyleROS(new java.math.BigDecimal("1.5"));
        result1.setType("core");
        result1.setStyleRevContribution(new java.math.BigDecimal("25.5"));
        result1.setTotalQuantitySold(100);
        result1.setTotalRevenue(new java.math.BigDecimal("5000.0"));
        result1.setDaysAvailable(30);
        result1.setDaysWithSales(25);
        result1.setAvgDiscount(new java.math.BigDecimal("8.5"));
        result1.setCalculatedDate(today);
        noosResultDao.insert(result1);

        // Results for yesterday
        NoosResult result2 = new NoosResult();
        result2.setCategory("PANTS");
        result2.setStyleCode("PANT001");
        result2.setStyleROS(new java.math.BigDecimal("2.1"));
        result2.setType("bestseller");
        result2.setStyleRevContribution(new java.math.BigDecimal("35.2"));
        result2.setTotalQuantitySold(150);
        result2.setTotalRevenue(new java.math.BigDecimal("7500.0"));
        result2.setDaysAvailable(30);
        result2.setDaysWithSales(28);
        result2.setAvgDiscount(new java.math.BigDecimal("5.0"));
        result2.setCalculatedDate(yesterday);
        noosResultDao.insert(result2);
    }

    /**
     * Create sample tasks for testing
     */
    private void createSampleTasks() {
        Date now = new Date();
        
        // Create completed task - don't set ID, let it be auto-generated
        Task task1 = new Task();
        task1.setTaskType("STYLES_UPLOAD"); // Use actual task type from service
        task1.setStatus("COMPLETED");
        task1.setStartTime(now);
        task1.setEndTime(now);
        // currentPhase removed - phase info now in progressMessage
//         task1.setMetadata("Test completed task");
        task1.setCancellationRequested(false);
        // Don't set createdDate - let @PrePersist handle it
        taskDao.insert(task1);

        // Create running task - don't set ID, let it be auto-generated
        Task task2 = new Task();
        task2.setTaskType("SALES_UPLOAD"); // Use actual task type from service
        task2.setStatus("RUNNING");
        task2.setStartTime(now);
        // currentPhase removed - phase info now in progressMessage
//         task2.setMetadata("Test running task");
        task2.setCancellationRequested(false);
        // Don't set createdDate - let @PrePersist handle it
        taskDao.insert(task2);
    }

    /**
     * Helper method to create TSV data from string
     */
    private java.util.ArrayList<java.util.HashMap<String, String>> createTsvDataFromString(String tsvData) {
        java.util.ArrayList<java.util.HashMap<String, String>> result = new java.util.ArrayList<>();
        String[] lines = tsvData.split("\n");
        if (lines.length < 2) return result;
        
        String[] headers = lines[0].split("\t");
        for (int i = 1; i < lines.length; i++) {
            String[] values = lines[i].split("\t");
            java.util.HashMap<String, String> row = new java.util.HashMap<>();
            for (int j = 0; j < Math.min(headers.length, values.length); j++) {
                row.put(headers[j], values[j]);
            }
            result.add(row);
        }
        return result;
    }
}
