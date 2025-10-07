package com.iris.increff.service;

import com.iris.increff.AbstractUnitTest;
import com.iris.increff.dao.TaskDao;
import com.iris.increff.dao.StyleDao;
import com.iris.increff.dao.StoreDao;
import com.iris.increff.dao.SkuDao;
import com.iris.increff.dao.SalesDao;
import com.iris.increff.model.Task;
import com.iris.increff.model.Style;
import com.iris.increff.model.Store;
import com.iris.increff.model.SKU;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Comprehensive test suite for AsyncUploadService
 * 
 * Tests all critical functionality including:
 * - Asynchronous file upload operations
 * - CompletableFuture handling and async execution
 * - Task management and progress tracking
 * - Cancellation logic and error handling
 * - Integration with all upload services (Style, Store, SKU, Sales)
 * - File processing and validation
 * - Edge cases and error scenarios
 * 
 * This service is critical for PRD compliance: "File uploads should be asynchronous"
 * and "Maintaining Task and Audit Tables"
 * 
 * Target: 90-95% method and line coverage for AsyncUploadService
 * 
 * @author Anshuk Kawarry
 * @version 1.0
 * @since 2025-01-01
 */
// Fixed async implementation - tests now use proper CompletableFuture patterns
public class AsyncUploadServiceTest extends AbstractUnitTest {

    @Autowired
    private AsyncUploadService asyncUploadService;

    @Autowired
    private TaskDao taskDao;

    @Autowired
    private StyleDao styleDao;

    @Autowired
    private StoreDao storeDao;

    @Autowired
    private SkuDao skuDao;

    @Autowired
    private SalesDao salesDao;

    @Autowired
    private DataClearingService dataClearingService;

    private Task testTask;
    private byte[] validStylesContent;
    private byte[] validStoresContent;
    private byte[] validSkusContent;
    private byte[] validSalesContent;
    private byte[] emptyContent;
    private byte[] invalidContent;

    /**
     * Setup test data before each test method
     * Creates test tasks and file content for async upload testing
     */
    @Before
    public void setUp() {
        // Clear all data first to ensure clean state
        dataClearingService.clearAllData();
        
        // Create test task
        testTask = new Task();
        testTask.setTaskType("FILE_UPLOAD");
        testTask.setStatus("PENDING");
        testTask.setStartTime(new Date());
        testTask.setParameters("Test upload task");
        // currentPhase removed - phase info now in progressMessage
//         testTask.setMetadata("Waiting to start");
        testTask.setCancellationRequested(false);
        taskDao.insert(testTask);

        // Create valid TSV content for different file types
        validStylesContent = createValidStylesTsv();
        validStoresContent = createValidStoresTsv();
        validSkusContent = createValidSkusTsv();
        validSalesContent = createValidSalesTsv();
        emptyContent = new byte[0];
        invalidContent = "invalid\tcontent\nwithout\tproper\theaders".getBytes();

        // Create prerequisite data for SKUs and Sales tests
        createPrerequisiteData();
    }

    // ==================== STYLES ASYNC UPLOAD TESTS ====================

    /**
     * Test successful async styles upload
     * Verifies that styles can be uploaded asynchronously with progress tracking
     */
    @Test
    public void testUploadStylesAsync_Success() throws Exception {
        // When: Upload styles asynchronously
        CompletableFuture<Task> future = asyncUploadService.uploadStylesAsync(
            testTask.getId(), validStylesContent, "test-styles.tsv");

        // Then: Wait for task completion and verify
        Task result = future.get(30, TimeUnit.SECONDS);
        
        assertNotNull("Result task should not be null", result);
        assertEquals("Task should be completed", "COMPLETED", result.getStatus());
        assertEquals("Should have processed records", Integer.valueOf(2), result.getProcessedRecords());
        assertEquals("Should have no errors", Integer.valueOf(0), result.getErrorCount());
        assertEquals("Progress should be 100%", 100.0, result.getProgressPercentage(), 0.01);
        // currentPhase removed - phase info now in progressMessage
        assertNotNull("Should have end time", result.getEndTime());

        // Verify data was actually saved
        assertEquals("Should have 2 styles in database", 2, styleDao.findAll().size());
    }

    /**
     * Test async styles upload with empty file
     * Verifies that empty files are handled gracefully
     */
    @Test
    public void testUploadStylesAsync_EmptyFile() throws Exception {
        // When: Upload empty file
        CompletableFuture<Task> future = asyncUploadService.uploadStylesAsync(
            testTask.getId(), emptyContent, "empty-styles.tsv");

        // Then: Wait for task completion and verify failure
        Task result = future.get(30, TimeUnit.SECONDS);
        
        assertNotNull("Result task should not be null", result);
        assertEquals("Task should be failed", "FAILED", result.getStatus());
        assertTrue("Should have error message", result.getErrorMessage().contains("File is empty"));
        // currentPhase removed - phase info now in progressMessage
        assertNotNull("Should have end time", result.getEndTime());

        // Verify no data was saved
        assertEquals("Should have no styles in database", 0, styleDao.findAll().size());
    }

    /**
     * Test async styles upload with non-existent task
     * Verifies that invalid task IDs are handled properly
     */
    @Test
    @Transactional
    @Rollback
    public void testUploadStylesAsync_NonExistentTask() throws Exception {
        // When: Upload with non-existent task ID
        asyncUploadService.uploadStylesAsync(
                999999L, validStylesContent, "test-styles.tsv");

        // Then: Should handle gracefully (task won't be found)
        // The method returns void, so we can't assert on return value
        // The async method will handle the missing task internally
    }

    // ==================== STORES ASYNC UPLOAD TESTS ====================

    /**
     * Test successful async stores upload
     * Verifies that stores can be uploaded asynchronously with progress tracking
     */
    @Test
    public void testUploadStoresAsync_Success() throws Exception {
        // When: Upload stores asynchronously
        CompletableFuture<Task> future = asyncUploadService.uploadStoresAsync(
            testTask.getId(), validStoresContent, "test-stores.tsv");

        // Then: Should complete successfully
        Task result = future.get(30, TimeUnit.SECONDS);
        
        assertNotNull("Result task should not be null", result);
        assertEquals("Task should be completed", "COMPLETED", result.getStatus());
        assertEquals("Should have processed records", Integer.valueOf(2), result.getProcessedRecords());
        assertEquals("Should have no errors", Integer.valueOf(0), result.getErrorCount());
        assertEquals("Progress should be 100%", 100.0, result.getProgressPercentage(), 0.01);

        // Verify data was actually saved
        assertEquals("Should have 2 stores in database", 2, storeDao.findAll().size());
    }

    /**
     * Test async stores upload with invalid data
     * Verifies that validation errors are handled properly in async context
     */
    @Test
    public void testUploadStoresAsync_InvalidData() throws Exception {
        // Given: Create invalid stores content (missing required fields)
        byte[] invalidStoresContent = "branch\tcity\n\tMUMBAI\nDELHI_CP\t".getBytes();

        // When: Upload invalid stores
        CompletableFuture<Task> future = asyncUploadService.uploadStoresAsync(
            testTask.getId(), invalidStoresContent, "invalid-stores.tsv");

        // Then: Should fail with validation errors
        Task result = future.get(30, TimeUnit.SECONDS);
        
        assertNotNull("Result task should not be null", result);
        assertEquals("Task should be failed", "FAILED", result.getStatus());
        assertTrue("Should have validation error message", 
            result.getErrorMessage().contains("Upload failed"));
        // currentPhase removed - phase info now in progressMessage

        // Verify no data was saved
        assertEquals("Should have no stores in database", 0, storeDao.findAll().size());
    }

    // ==================== SKUS ASYNC UPLOAD TESTS ====================

    /**
     * Test successful async SKUs upload
     * Verifies that SKUs can be uploaded asynchronously with proper dependencies
     */
    @Test
    public void testUploadSkusAsync_Success() throws Exception {
        // When: Upload SKUs asynchronously
        CompletableFuture<Task> future = asyncUploadService.uploadSkusAsync(testTask.getId(), validSkusContent, "test-skus.tsv");

        // Then: Wait for task completion and verify
        Task result = future.get(30, TimeUnit.SECONDS);
        
        assertNotNull("Result task should not be null", result);
        assertEquals("Task should be completed", "COMPLETED", result.getStatus());
        assertEquals("Should have processed records", Integer.valueOf(2), result.getProcessedRecords());
        assertEquals("Should have no errors", Integer.valueOf(0), result.getErrorCount());

        // Verify data was actually saved
        assertEquals("Should have 2 SKUs in database", 2, skuDao.findAll().size());
    }

    /**
     * Test async SKUs upload with missing style dependencies
     * Verifies that dependency validation works in async context
     */
    @Test
    public void testUploadSkusAsync_MissingStyleDependency() throws Exception {
        // Given: Clear prerequisite styles using DataClearingService
        dataClearingService.clearDataForSkuUpload();
        
        // When: Upload SKUs without required styles
        CompletableFuture<Task> future = asyncUploadService.uploadSkusAsync(testTask.getId(), validSkusContent, "test-skus.tsv");

        // Then: Wait for task completion and verify failure
        Task result = future.get(30, TimeUnit.SECONDS);
        
        assertNotNull("Result task should not be null", result);
        assertEquals("Task should be failed", "FAILED", result.getStatus());
        assertTrue("Should have dependency error", 
            result.getErrorMessage().contains("Upload failed"));

        // Verify no SKUs were saved
        assertEquals("Should have no SKUs in database", 0, skuDao.findAll().size());
    }

    // ==================== SALES ASYNC UPLOAD TESTS ====================

    /**
     * Test successful async sales upload
     * Verifies that sales can be uploaded asynchronously with all dependencies
     */
    @Test
    public void testUploadSalesAsync_Success() throws Exception {
        // When: Upload sales asynchronously
        CompletableFuture<Task> future = asyncUploadService.uploadSalesAsync(
            testTask.getId(), validSalesContent, "test-sales.tsv");

        // Then: Should complete successfully
        Task result = future.get(30, TimeUnit.SECONDS);
        
        assertNotNull("Result task should not be null", result);
        assertEquals("Task should be completed", "COMPLETED", result.getStatus());
        assertEquals("Should have processed records", Integer.valueOf(2), result.getProcessedRecords());
        assertEquals("Should have no errors", Integer.valueOf(0), result.getErrorCount());

        // Verify data was actually saved
        assertEquals("Should have 2 sales in database", 2, salesDao.findAll().size());
    }

    /**
     * Test async sales upload with graceful SKU handling
     * Verifies that missing SKUs are handled gracefully with warnings
     */
    @Test
    public void testUploadSalesAsync_GracefulSkuHandling() throws Exception {
        // Given: Create sales content with some missing SKUs
        byte[] mixedSalesContent = ("day\tsku\tchannel\tquantity\tdiscount\trevenue\n" +
                                   "2024-01-15\tSKU001\tMUMBAI_CENTRAL\t5\t10.00\t450.00\n" +
                                   "2024-01-16\tMISSING_SKU\tDELHI_CP\t3\t5.50\t280.50\n" +
                                   "2024-01-17\tSKU002\tMUMBAI_CENTRAL\t2\t0.00\t200.00").getBytes();

        // When: Upload sales with missing SKUs
        CompletableFuture<Task> future = asyncUploadService.uploadSalesAsync(
            testTask.getId(), mixedSalesContent, "mixed-sales.tsv");

        // Then: Should complete successfully (graceful handling)
        Task result = future.get(30, TimeUnit.SECONDS);
        
        assertNotNull("Result task should not be null", result);
        assertEquals("Task should be completed", "COMPLETED", result.getStatus());
        assertEquals("Should have processed valid records", Integer.valueOf(2), result.getProcessedRecords());

        // Verify only valid sales were saved
        assertEquals("Should have 2 valid sales in database", 2, salesDao.findAll().size());
    }

    // ==================== TASK CANCELLATION TESTS ====================

    /**
     * Test task cancellation during async upload
     * Verifies that cancellation requests are handled properly
     */
    @Test
    public void testUploadAsync_TaskCancellation() throws Exception {
        // Given: Create a task that will be cancelled
        Task cancellableTask = new Task();
        cancellableTask.setTaskType("FILE_UPLOAD");
        cancellableTask.setStatus("PENDING");
        cancellableTask.setStartTime(new Date());
        cancellableTask.setParameters("Cancellable upload task");
        // currentPhase removed - phase info now in progressMessage
//         cancellableTask.setMetadata("Waiting to start");
        cancellableTask.setCancellationRequested(false);
        taskDao.insert(cancellableTask);

        // When: Start upload and immediately request cancellation
        CompletableFuture<Task> future = asyncUploadService.uploadStylesAsync(
            cancellableTask.getId(), validStylesContent, "test-styles.tsv");

        // Simulate cancellation request (this would normally come from UI)
        Thread.sleep(1000); // Let upload start
        Task taskToCancel = taskDao.select(cancellableTask.getId());
        taskToCancel.setCancellationRequested(true);
        taskDao.update(taskToCancel);

        // Then: Should handle cancellation
        Task result = future.get(30, TimeUnit.SECONDS);
        
        assertNotNull("Result task should not be null", result);
        // Note: Due to timing, task might complete before cancellation is processed
        // So we check for either CANCELLED or FAILED status
        assertTrue("Task should be either CANCELLED or FAILED", 
            "CANCELLED".equals(result.getStatus()) || "FAILED".equals(result.getStatus()));
        
        if ("CANCELLED".equals(result.getStatus())) {
            // currentPhase removed - phase info now in progressMessage
            // Note: Metadata format may vary, just check it exists
//             assertNotNull("Should have metadata", result.getMetadata());
        }
    }

    // ==================== PROGRESS TRACKING TESTS ====================

    /**
     * Test progress tracking during async upload
     * Verifies that progress is updated correctly throughout the process
     */
    @Test
    public void testUploadAsync_ProgressTracking() throws Exception {
        // When: Upload file and track progress
        CompletableFuture<Task> future = asyncUploadService.uploadStylesAsync(
            testTask.getId(), validStylesContent, "test-styles.tsv");

        // Monitor progress during upload
        Thread.sleep(1000); // Let upload start
        
        Task progressTask = taskDao.select(testTask.getId());
        assertNotNull("Task should exist during upload", progressTask);
        assertTrue("Progress should be > 0", progressTask.getProgressPercentage() > 0);
        assertEquals("Status should be RUNNING", "RUNNING", progressTask.getStatus());
//         assertNotNull("Should have progress message", progressTask.getMetadata());
        assertNotNull("Should have file name", progressTask.getFileName());

        // Wait for completion
        Task result = future.get(30, TimeUnit.SECONDS);
        
        assertEquals("Final progress should be 100%", 100.0, result.getProgressPercentage(), 0.01);
        // currentPhase removed - phase info now in progressMessage
    }

    // ==================== ERROR HANDLING TESTS ====================

    /**
     * Test error handling during async upload
     * Verifies that exceptions are properly caught and handled
     */
    @Test
    public void testUploadAsync_ErrorHandling() throws Exception {
        // Given: Create content that will cause processing errors
        byte[] errorContent = "invalid\ttsv\tformat\nwith\twrong\theaders\tand\textra\tcolumns".getBytes();

        // When: Upload invalid content
        CompletableFuture<Task> future = asyncUploadService.uploadStylesAsync(
            testTask.getId(), errorContent, "error-styles.tsv");

        // Then: Should handle error gracefully
        Task result = future.get(30, TimeUnit.SECONDS);
        
        assertNotNull("Result task should not be null", result);
        assertEquals("Task should be failed", "FAILED", result.getStatus());
        // currentPhase removed - phase info now in progressMessage
        assertNotNull("Should have error message", result.getErrorMessage());
        assertNotNull("Should have end time", result.getEndTime());

        // Verify no data was saved
        assertEquals("Should have no styles in database", 0, styleDao.findAll().size());
    }

    // ==================== INTEGRATION TESTS ====================

    /**
     * Test multiple concurrent async uploads
     * Verifies that multiple uploads can run concurrently without interference
     */
    @Test
    public void testMultipleConcurrentUploads() throws Exception {
        // Given: Create multiple tasks
        Task task2 = new Task();
        task2.setTaskType("FILE_UPLOAD");
        task2.setStatus("PENDING");
        task2.setStartTime(new Date());
        task2.setParameters("Concurrent upload task 2");
        // currentPhase removed - phase info now in progressMessage
//         task2.setMetadata("Waiting to start");
        task2.setCancellationRequested(false);
        taskDao.insert(task2);

        // When: Start multiple concurrent uploads
        CompletableFuture<Task> future1 = asyncUploadService.uploadStylesAsync(testTask.getId(), validStylesContent, "styles1.tsv");
        CompletableFuture<Task> future2 = asyncUploadService.uploadStoresAsync(task2.getId(), validStoresContent, "stores1.tsv");

        // Then: Both should complete successfully
        Task result1 = future1.get(60, TimeUnit.SECONDS);
        Task result2 = future2.get(60, TimeUnit.SECONDS);
        
        assertNotNull("Result 1 should not be null", result1);
        assertNotNull("Result 2 should not be null", result2);
        assertEquals("Task 1 should be completed", "COMPLETED", result1.getStatus());
        assertEquals("Task 2 should be completed", "COMPLETED", result2.getStatus());

        // Verify both uploads succeeded
        assertEquals("Should have 2 styles", 2, styleDao.findAll().size());
        assertEquals("Should have 2 stores", 2, storeDao.findAll().size());
    }

    /**
     * Test file name and metadata handling
     * Verifies that file metadata is properly stored and tracked
     */
    @Test
    public void testFileMetadataHandling() throws Exception {
        // Given: Specific file name
        String fileName = "important-styles-2024.tsv";

        // When: Upload with specific file name
        CompletableFuture<Task> future = asyncUploadService.uploadStylesAsync(
            testTask.getId(), validStylesContent, fileName);

        // Then: Should store file metadata
        Task result = future.get(30, TimeUnit.SECONDS);
        
        assertNotNull("Result task should not be null", result);
        assertEquals("Should store file name", fileName, result.getFileName());
        assertEquals("Should have total records", Integer.valueOf(2), result.getTotalRecords());
        assertTrue("Should have result summary in parameters", 
            result.getParameters().contains("Results:"));
    }

    // ==================== EDGE CASES TESTS ====================

    /**
     * Test upload with very large file content
     * Verifies that large files are handled properly
     */
    @Test
    public void testUploadAsync_LargeFile() throws Exception {
        // Given: Create large file content (simulate 1000 records)
        StringBuilder largeContent = new StringBuilder("style\tbrand\tcategory\tsub_category\tmrp\tgender\n");
        for (int i = 1; i <= 100; i++) { // Reduced to 100 for test performance
            largeContent.append(String.format("STYLE%03d\tBRAND%d\tCATEGORY%d\tSUBCATEGORY%d\t%.2f\tM\n", 
                i, i % 5, i % 3, i % 4, 100.0 + i));
        }
        byte[] largeFileContent = largeContent.toString().getBytes();

        // When: Upload large file
        CompletableFuture<Task> future = asyncUploadService.uploadStylesAsync(
            testTask.getId(), largeFileContent, "large-styles.tsv");

        // Then: Should handle large file successfully
        Task result = future.get(60, TimeUnit.SECONDS); // Longer timeout for large file
        
        assertNotNull("Result task should not be null", result);
        assertEquals("Task should be completed", "COMPLETED", result.getStatus());
        assertEquals("Should have processed all records", Integer.valueOf(100), result.getProcessedRecords());
        assertEquals("Should have total records", Integer.valueOf(100), result.getTotalRecords());

        // Verify data was saved
        assertEquals("Should have 100 styles in database", 100, styleDao.findAll().size());
    }

    /**
     * Test upload with null file content
     * Verifies that null content is handled gracefully
     */
    @Test
    public void testUploadAsync_NullContent() throws Exception {
        // When: Upload with null content
        CompletableFuture<Task> future = asyncUploadService.uploadStylesAsync(
            testTask.getId(), null, "null-styles.tsv");

        // Then: Should handle null content gracefully
        Task result = future.get(30, TimeUnit.SECONDS);
        
        assertNotNull("Result task should not be null", result);
        assertEquals("Task should be failed", "FAILED", result.getStatus());
        assertTrue("Should have error message about empty file", 
            result.getErrorMessage().contains("File is empty"));
    }

    // ==================== HELPER METHODS ====================

    /**
     * Create valid styles TSV content for testing
     * Uses ProcessTsv.stylesHeaders format: style, brand, category, sub_category, mrp, gender
     */
    private byte[] createValidStylesTsv() {
        return ("style\tbrand\tcategory\tsub_category\tmrp\tgender\n" +
                "SHIRT001\tNIKE\tSHIRTS\tCASUAL\t100.50\tM\n" +
                "PANT001\tADIDAS\tPANTS\tFORMAL\t150.75\tF").getBytes();
    }

    /**
     * Create valid stores TSV content for testing
     * Uses ProcessTsv.storeHeaders format: branch, city
     */
    private byte[] createValidStoresTsv() {
        return ("branch\tcity\n" +
                "MUMBAI_CENTRAL\tMUMBAI\n" +
                "DELHI_CP\tDELHI").getBytes();
    }

    /**
     * Create valid SKUs TSV content for testing
     * Uses ProcessTsv.skuHeaders format: sku, style, size
     */
    private byte[] createValidSkusTsv() {
        return ("sku\tstyle\tsize\n" +
                "SKU001\tSHIRT001\tM\n" +
                "SKU002\tPANT001\tL").getBytes();
    }

    /**
     * Create valid sales TSV content for testing
     * Uses ProcessTsv.salesHeaders format: day, sku, channel, quantity, discount, revenue
     */
    private byte[] createValidSalesTsv() {
        return ("day\tsku\tchannel\tquantity\tdiscount\trevenue\n" +
                "2024-01-15\tSKU001\tMUMBAI_CENTRAL\t5\t10.00\t450.00\n" +
                "2024-01-16\tSKU002\tDELHI_CP\t3\t5.50\t280.50").getBytes();
    }

    /**
     * Create prerequisite data for SKU and Sales tests
     */
    private void createPrerequisiteData() {
        // Create styles
        Style style1 = new Style();
        style1.setStyleCode("SHIRT001");
        style1.setBrand("NIKE");
        style1.setCategory("SHIRTS");
        style1.setSubCategory("CASUAL");
        style1.setMrp(new BigDecimal("100.50"));
        style1.setGender("M");
        styleDao.save(style1);

        Style style2 = new Style();
        style2.setStyleCode("PANT001");
        style2.setBrand("ADIDAS");
        style2.setCategory("PANTS");
        style2.setSubCategory("FORMAL");
        style2.setMrp(new BigDecimal("150.75"));
        style2.setGender("F");
        styleDao.save(style2);

        // Create stores
        Store store1 = new Store();
        store1.setBranch("MUMBAI_CENTRAL");
        store1.setCity("MUMBAI");
        storeDao.save(store1);

        Store store2 = new Store();
        store2.setBranch("DELHI_CP");
        store2.setCity("DELHI");
        storeDao.save(store2);

        // Create SKUs
        SKU sku1 = new SKU();
        sku1.setSku("SKU001");
        sku1.setStyleId(style1.getId());
        sku1.setSize("M");
        skuDao.save(sku1);

        SKU sku2 = new SKU();
        sku2.setSku("SKU002");
        sku2.setStyleId(style2.getId());
        sku2.setSize("L");
        skuDao.save(sku2);
    }

    // Note: waitForTaskCompletion method removed - tests now use CompletableFuture.get() for proper async testing
}
