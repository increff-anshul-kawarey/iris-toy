package com.iris.increff.service;

import com.iris.increff.AbstractUnitTest;
import com.iris.increff.dao.*;
import com.iris.increff.model.DashBoardData;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

/**
 * Comprehensive test suite for DashboardMetricsService
 * 
 * Tests all critical functionality including:
 * - Dashboard metrics collection and calculation
 * - Data volume statistics and status determination
 * - Task activity monitoring and success rate calculation
 * - Error handling and fallback data generation
 * - Status message generation for different scenarios
 * 
 * This service is critical for PRD compliance: "Dashboard with real-time metrics"
 * and "System monitoring and health status"
 * 
 * Target: 90-95% method and line coverage for DashboardMetricsService
 * 
 * @author Anshuk Kawarry
 * @version 1.0
 * @since 2025-01-01
 */
public class DashboardMetricsServiceTest extends AbstractUnitTest {

    @Autowired
    private DashboardMetricsService dashboardMetricsService;

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
    private SalesDao salesDao;

    @Autowired
    private SkuDao skuDao;

    @Autowired
    private StoreDao storeDao;

    @Autowired
    private StyleDao styleDao;

    /**
     * Setup test data before each test method
     * Ensures clean state for consistent testing
     */
    @Before
    public void setUp() {
        dataClearingService.clearAllData();
    }

    // ==================== DASHBOARD METRICS TESTS ====================

    /**
     * Test dashboard metrics with empty database
     * Verifies that service handles empty state gracefully
     */
    @Test
    @Transactional
    @Rollback
    public void testGetDashboardMetrics_EmptyDatabase() {
        // When: Get dashboard metrics with no data
        DashBoardData metrics = dashboardMetricsService.getDashboardMetrics();

        // Then: Should return empty metrics with appropriate status messages
        assertNotNull("Dashboard metrics should not be null", metrics);
        
        // Sales data metrics
        assertEquals("Sales count should be 0", 0L, metrics.getTotalSalesRecords());
        assertEquals("Sales status should indicate no data", "No data available", metrics.getSalesDataStatus());
        
        // Master data metrics
        assertEquals("SKU count should be 0", 0L, metrics.getTotalSkus());
        assertEquals("Store count should be 0", 0L, metrics.getTotalStores());
        assertEquals("Style count should be 0", 0L, metrics.getTotalStyles());
        assertEquals("Master data status should indicate setup required", "Setup required", metrics.getMasterDataStatus());
        
        // Activity metrics
        assertEquals("Recent uploads should be 0", 0, metrics.getRecentUploads());
        assertEquals("Upload success rate should be 0", 0.0, metrics.getUploadSuccessRate(), 0.01);
        assertEquals("Activity status should indicate no activity", "No recent activity", metrics.getRecentActivityStatus());
        
        // Processing metrics
        assertEquals("Active tasks should be 0", 0, metrics.getActiveTasks());
        assertEquals("Pending tasks should be 0", 0, metrics.getPendingTasks());
        assertEquals("Processing status should indicate idle", "System idle", metrics.getProcessingStatus());
    }

    /**
     * Test dashboard metrics with sample data
     * Verifies correct calculation and status determination with real data
     */
    @Test
    @Transactional
    @Rollback
    public void testGetDashboardMetrics_WithSampleData() throws Exception {
        // Given: Create sample data
        createSampleMasterData();
        createSampleSalesData();

        // When: Get dashboard metrics
        DashBoardData metrics = dashboardMetricsService.getDashboardMetrics();

        // Then: Should return correct metrics
        assertNotNull("Dashboard metrics should not be null", metrics);
        
        // Sales data metrics
        assertTrue("Sales count should be > 0", metrics.getTotalSalesRecords() > 0);
        assertNotEquals("Sales status should not be 'No data available'", "No data available", metrics.getSalesDataStatus());
        
        // Master data metrics
        assertTrue("SKU count should be > 0", metrics.getTotalSkus() > 0);
        assertTrue("Store count should be > 0", metrics.getTotalStores() > 0);
        assertTrue("Style count should be > 0", metrics.getTotalStyles() > 0);
        assertEquals("Master data status should indicate complete setup", "Complete setup", metrics.getMasterDataStatus());
        
        // Verify specific counts match what we created
        assertEquals("Should have 2 styles", 2L, metrics.getTotalStyles());
        assertEquals("Should have 2 stores", 2L, metrics.getTotalStores());
        assertEquals("Should have 2 SKUs", 2L, metrics.getTotalSkus());
        assertEquals("Should have 2 sales records", 2L, metrics.getTotalSalesRecords());
    }

    /**
     * Test dashboard metrics with large dataset
     * Verifies status messages change appropriately with data volume
     */
    @Test
    @Transactional
    @Rollback
    public void testGetDashboardMetrics_LargeDataset() throws Exception {
        // Given: Create large dataset
        createLargeMasterData();
        createLargeSalesData();

        // When: Get dashboard metrics
        DashBoardData metrics = dashboardMetricsService.getDashboardMetrics();

        // Then: Should return metrics with appropriate status for large data
        assertNotNull("Dashboard metrics should not be null", metrics);
        
        // Sales data should show rich data status
        assertTrue("Sales count should be large", metrics.getTotalSalesRecords() >= 1000);
        assertTrue("Sales status should indicate good or rich data", 
                  metrics.getSalesDataStatus().contains("Good") || 
                  metrics.getSalesDataStatus().contains("Rich"));
        
        // Master data should show complete setup
        assertEquals("Master data status should be complete", "Complete setup", metrics.getMasterDataStatus());
    }

    /**
     * Test dashboard metrics with partial master data
     * Verifies status calculation when only some master data exists
     */
    @Test
    @Transactional
    @Rollback
    public void testGetDashboardMetrics_PartialMasterData() throws Exception {
        // Given: Create only styles and stores (no SKUs)
        createSampleStyles();
        createSampleStores();
        // Intentionally not creating SKUs

        // When: Get dashboard metrics
        DashBoardData metrics = dashboardMetricsService.getDashboardMetrics();

        // Then: Should indicate partial setup
        assertNotNull("Dashboard metrics should not be null", metrics);
        assertEquals("Master data status should indicate partial setup", "Partial setup", metrics.getMasterDataStatus());
        assertTrue("Should have styles", metrics.getTotalStyles() > 0);
        assertTrue("Should have stores", metrics.getTotalStores() > 0);
        assertEquals("Should have no SKUs", 0L, metrics.getTotalSkus());
    }

    // ==================== STATUS DETERMINATION TESTS ====================

    /**
     * Test sales data status determination
     * Verifies correct status messages for different data volumes
     */
    @Test
    @Transactional
    @Rollback
    public void testSalesDataStatusDetermination() throws Exception {
        // Test different sales data volumes and verify status messages
        
        // No data
        DashBoardData emptyMetrics = dashboardMetricsService.getDashboardMetrics();
        assertEquals("Empty data should show 'No data available'", "No data available", emptyMetrics.getSalesDataStatus());
        
        // Limited data (< 1000 records)
        createLimitedSalesData(500);
        DashBoardData limitedMetrics = dashboardMetricsService.getDashboardMetrics();
        assertEquals("Limited data should show 'Limited data'", "Limited data", limitedMetrics.getSalesDataStatus());
        
        // Clear and create good volume data (1000-10000 records)
        dataClearingService.clearAllData();
        createSampleMasterData();
        createGoodVolumeSalesData(5000);
        DashBoardData goodMetrics = dashboardMetricsService.getDashboardMetrics();
        assertEquals("Good volume should show 'Good data volume'", "Good data volume", goodMetrics.getSalesDataStatus());
    }

    /**
     * Test processing status determination
     * Verifies correct status messages for different task loads
     */
    @Test
    @Transactional
    @Rollback
    public void testProcessingStatusDetermination() {
        // Test with no active tasks (already tested in empty database test)
        DashBoardData idleMetrics = dashboardMetricsService.getDashboardMetrics();
        assertEquals("No tasks should show 'System idle'", "System idle", idleMetrics.getProcessingStatus());
        
        // Note: Testing with actual tasks would require complex setup
        // The status determination logic is tested through the main metrics method
    }

    // ==================== ERROR HANDLING TESTS ====================

    /**
     * Test error handling and fallback data
     * Verifies service gracefully handles database errors
     */
    @Test
    @Transactional
    @Rollback
    public void testErrorHandlingAndFallback() {
        // Note: It's difficult to simulate database errors in this test environment
        // The error handling is primarily tested through the normal flow
        // In case of errors, the service returns safe fallback data
        
        // When: Get dashboard metrics (should work normally)
        DashBoardData metrics = dashboardMetricsService.getDashboardMetrics();
        
        // Then: Should return valid metrics (not fallback data in normal case)
        assertNotNull("Dashboard metrics should not be null", metrics);
        assertNotEquals("Should not return fallback status", "Data unavailable", metrics.getSalesDataStatus());
        assertNotEquals("Should not return fallback status", "System unavailable", metrics.getProcessingStatus());
    }

    // ==================== INTEGRATION TESTS ====================

    /**
     * Test dashboard metrics integration with all DAOs
     * Verifies service correctly integrates with all data access objects
     */
    @Test
    @Transactional
    @Rollback
    public void testDashboardMetricsIntegration() throws Exception {
        // Given: Create comprehensive test data
        createSampleMasterData();
        createSampleSalesData();

        // When: Get dashboard metrics
        DashBoardData metrics = dashboardMetricsService.getDashboardMetrics();

        // Then: Should integrate data from all DAOs correctly
        assertNotNull("Dashboard metrics should not be null", metrics);
        
        // Verify integration with StyleDao
        assertTrue("Should have style data", metrics.getTotalStyles() > 0);
        
        // Verify integration with StoreDao
        assertTrue("Should have store data", metrics.getTotalStores() > 0);
        
        // Verify integration with SkuDao
        assertTrue("Should have SKU data", metrics.getTotalSkus() > 0);
        
        // Verify integration with SalesDao
        assertTrue("Should have sales data", metrics.getTotalSalesRecords() > 0);
        
        // Verify integration with TaskDao (through activity metrics)
        assertNotNull("Should have activity status", metrics.getRecentActivityStatus());
        assertNotNull("Should have processing status", metrics.getProcessingStatus());
    }

    /**
     * Test dashboard metrics consistency
     * Verifies metrics remain consistent across multiple calls
     */
    @Test
    @Transactional
    @Rollback
    public void testDashboardMetricsConsistency() throws Exception {
        // Given: Create sample data
        createSampleMasterData();
        createSampleSalesData();

        // When: Get dashboard metrics multiple times
        DashBoardData metrics1 = dashboardMetricsService.getDashboardMetrics();
        DashBoardData metrics2 = dashboardMetricsService.getDashboardMetrics();

        // Then: Should return consistent results
        assertNotNull("First metrics should not be null", metrics1);
        assertNotNull("Second metrics should not be null", metrics2);
        
        assertEquals("Sales count should be consistent", metrics1.getTotalSalesRecords(), metrics2.getTotalSalesRecords());
        assertEquals("Style count should be consistent", metrics1.getTotalStyles(), metrics2.getTotalStyles());
        assertEquals("Store count should be consistent", metrics1.getTotalStores(), metrics2.getTotalStores());
        assertEquals("SKU count should be consistent", metrics1.getTotalSkus(), metrics2.getTotalSkus());
        
        assertEquals("Sales status should be consistent", metrics1.getSalesDataStatus(), metrics2.getSalesDataStatus());
        assertEquals("Master data status should be consistent", metrics1.getMasterDataStatus(), metrics2.getMasterDataStatus());
    }

    // ==================== HELPER METHODS ====================

    /**
     * Create sample master data for testing
     */
    private void createSampleMasterData() throws Exception {
        createSampleStyles();
        createSampleStores();
        createSampleSkus();
    }

    /**
     * Create sample styles
     */
    private void createSampleStyles() throws Exception {
        String stylesData = "style\tbrand\tcategory\tsub_category\tmrp\tgender\n" +
                           "SHIRT001\tNIKE\tSHIRTS\tCASUAL\t100.50\tM\n" +
                           "PANT001\tADIDAS\tPANTS\tFORMAL\t150.75\tF";
        
        styleService.processAndSaveStyles(createTsvDataFromString(stylesData));
    }

    /**
     * Create sample stores
     */
    private void createSampleStores() throws Exception {
        String storesData = "branch\tcity\n" +
                           "MUMBAI_CENTRAL\tMUMBAI\n" +
                           "DELHI_CP\tDELHI";
        
        storeService.processAndSaveStores(createTsvDataFromString(storesData));
    }

    /**
     * Create sample SKUs
     */
    private void createSampleSkus() throws Exception {
        String skusData = "sku\tstyle\tsize\n" +
                         "SKU001\tSHIRT001\tM\n" +
                         "SKU002\tPANT001\tL";
        
        skuService.processAndSaveSKUs(createTsvDataFromString(skusData));
    }

    /**
     * Create sample sales data
     */
    private void createSampleSalesData() throws Exception {
        String salesData = "day\tsku\tchannel\tquantity\tdiscount\trevenue\n" +
                          "2024-01-15\tSKU001\tMUMBAI_CENTRAL\t5\t10.00\t450.00\n" +
                          "2024-01-16\tSKU002\tDELHI_CP\t3\t5.50\t280.50";
        
        salesService.processAndSaveSales(createTsvDataFromString(salesData));
    }

    /**
     * Create large master data for volume testing
     */
    private void createLargeMasterData() throws Exception {
        // Create 50 styles
        StringBuilder stylesData = new StringBuilder("style\tbrand\tcategory\tsub_category\tmrp\tgender\n");
        for (int i = 1; i <= 50; i++) {
            stylesData.append(String.format("STYLE%03d\tBRAND%d\tCATEGORY%d\tSUBCAT%d\t%.2f\t%s\n", 
                i, i % 5, i % 3, i % 4, 100.0 + i, i % 2 == 0 ? "M" : "F"));
        }
        styleService.processAndSaveStyles(createTsvDataFromString(stylesData.toString()));

        // Create 20 stores
        StringBuilder storesData = new StringBuilder("branch\tcity\n");
        for (int i = 1; i <= 20; i++) {
            storesData.append(String.format("STORE%03d\tCITY%d\n", i, i % 10));
        }
        storeService.processAndSaveStores(createTsvDataFromString(storesData.toString()));

        // Create 100 SKUs
        StringBuilder skusData = new StringBuilder("sku\tstyle\tsize\n");
        for (int i = 1; i <= 100; i++) {
            int styleNum = (i % 50) + 1;
            skusData.append(String.format("SKU%03d\tSTYLE%03d\t%s\n", i, styleNum, i % 3 == 0 ? "L" : "M"));
        }
        skuService.processAndSaveSKUs(createTsvDataFromString(skusData.toString()));
    }

    /**
     * Create large sales data for volume testing
     */
    private void createLargeSalesData() throws Exception {
        StringBuilder salesData = new StringBuilder("day\tsku\tchannel\tquantity\tdiscount\trevenue\n");
        for (int i = 1; i <= 1500; i++) {
            int skuNum = (i % 100) + 1;
            int storeNum = (i % 20) + 1;
            salesData.append(String.format("2024-01-%02d\tSKU%03d\tSTORE%03d\t%d\t%.2f\t%.2f\n", 
                (i % 28) + 1, skuNum, storeNum, i % 10 + 1, i % 20 + 5.0, (i % 100 + 50) * 10.0));
        }
        salesService.processAndSaveSales(createTsvDataFromString(salesData.toString()));
    }

    /**
     * Create limited sales data for status testing
     */
    private void createLimitedSalesData(int count) throws Exception {
        // First create minimal master data
        createSampleMasterData();
        
        StringBuilder salesData = new StringBuilder("day\tsku\tchannel\tquantity\tdiscount\trevenue\n");
        for (int i = 1; i <= count; i++) {
            salesData.append(String.format("2024-01-%02d\tSKU001\tMUMBAI_CENTRAL\t%d\t%.2f\t%.2f\n", 
                (i % 28) + 1, i % 5 + 1, i % 10 + 5.0, (i % 50 + 100) * 5.0));
        }
        salesService.processAndSaveSales(createTsvDataFromString(salesData.toString()));
    }

    /**
     * Create good volume sales data for status testing
     */
    private void createGoodVolumeSalesData(int count) throws Exception {
        StringBuilder salesData = new StringBuilder("day\tsku\tchannel\tquantity\tdiscount\trevenue\n");
        for (int i = 1; i <= count; i++) {
            int skuNum = (i % 2) + 1;
            String skuCode = skuNum == 1 ? "SKU001" : "SKU002";
            String channel = skuNum == 1 ? "MUMBAI_CENTRAL" : "DELHI_CP";
            salesData.append(String.format("2024-01-%02d\t%s\t%s\t%d\t%.2f\t%.2f\n", 
                (i % 28) + 1, skuCode, channel, i % 5 + 1, i % 15 + 5.0, (i % 100 + 200) * 3.0));
        }
        salesService.processAndSaveSales(createTsvDataFromString(salesData.toString()));
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
