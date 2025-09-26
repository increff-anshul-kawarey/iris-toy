package com.iris.increff.service;

import com.iris.increff.controller.UploadResponse;
import com.iris.increff.dao.TaskDao;
import com.iris.increff.model.Task;
import com.iris.increff.util.ApiException;
import com.iris.increff.util.ProcessTsv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

/**
 * Async Upload Service for File Processing
 * 
 * Handles asynchronous file uploads with progress tracking for:
 * - Styles TSV files
 * - Stores TSV files  
 * - SKUs TSV files
 * - Sales TSV files
 * 
 * PRD Requirement: "File uploads and downloads should be asynchronous"
 * PRD Requirement: "Maintaining Task and Audit Tables"
 * 
 * @author Anshuk Kawarry
 * @version 1.0
 * @since 2025-01-01
 */
@Service
public class AsyncUploadService {

    private static final Logger logger = LoggerFactory.getLogger(AsyncUploadService.class);

    @Autowired
    private TaskDao taskDao;

    @Autowired
    private StyleService styleService;
    
    @Autowired
    private StoreService storeService;
    
    @Autowired
    private SkuService skuService;
    
    @Autowired
    private SalesService salesService;

    /**
     * Async Styles Upload with Progress Tracking
     * 
     * @param taskId Task ID for progress tracking
     * @param fileContent Pre-read file content as byte array
     * @param fileName Original file name
     * @return CompletableFuture for async execution
     */
    @Async("fileExecutor")
    @Transactional
    public CompletableFuture<Task> uploadStylesAsync(Long taskId, byte[] fileContent, String fileName) {
        return processFileAsync(taskId, fileContent, fileName, "STYLES", ProcessTsv.stylesHeaders, 
                               (data) -> styleService.processAndSaveStyles(data));
    }

    /**
     * Async Stores Upload with Progress Tracking
     * 
     * @param taskId Task ID for progress tracking
     * @param fileContent Pre-read file content as byte array
     * @param fileName Original file name
     * @return CompletableFuture for async execution
     */
    @Async("fileExecutor")
    @Transactional
    public CompletableFuture<Task> uploadStoresAsync(Long taskId, byte[] fileContent, String fileName) {
        return processFileAsync(taskId, fileContent, fileName, "STORES", ProcessTsv.storeHeaders,
                               (data) -> storeService.processAndSaveStores(data));
    }

    /**
     * Async SKUs Upload with Progress Tracking
     * 
     * @param taskId Task ID for progress tracking
     * @param fileContent Pre-read file content as byte array
     * @param fileName Original file name
     * @return CompletableFuture for async execution
     */
    @Async("fileExecutor")
    @Transactional
    public CompletableFuture<Task> uploadSkusAsync(Long taskId, byte[] fileContent, String fileName) {
        return processFileAsync(taskId, fileContent, fileName, "SKUS", ProcessTsv.skuHeaders,
                               (data) -> skuService.processAndSaveSKUs(data));
    }

    /**
     * Async Sales Upload with Progress Tracking
     * 
     * @param taskId Task ID for progress tracking
     * @param fileContent Pre-read file content as byte array
     * @param fileName Original file name
     * @return CompletableFuture for async execution
     */
    @Async("fileExecutor")
    @Transactional
    public CompletableFuture<Task> uploadSalesAsync(Long taskId, byte[] fileContent, String fileName) {
        return processFileAsync(taskId, fileContent, fileName, "SALES", ProcessTsv.salesHeaders,
                               (data) -> salesService.processAndSaveSales(data));
    }

    /**
     * Generic async file processing with progress tracking
     * 
     * @param taskId Task ID for progress tracking
     * @param fileContent Pre-read file content as byte array
     * @param fileName Original file name
     * @param fileType Type of file (STYLES, STORES, etc.)
     * @param headers Expected TSV headers
     * @param processor Function to process the parsed data
     * @return CompletableFuture with task result
     */
    private CompletableFuture<Task> processFileAsync(Long taskId, byte[] fileContent, String fileName, String fileType, 
                                                   String[] headers, FileProcessor processor) {
        Task task = taskDao.select(taskId);
        if (task == null) {
            logger.error("❌ Task not found: {}", taskId);
            return CompletableFuture.completedFuture(null);
        }

        System.out.println("📁 SYSTEM.OUT: Starting async " + fileType + " upload for task: " + taskId);
        System.out.println("🔄 SYSTEM.OUT: Thread: " + Thread.currentThread().getName() + ", File: " + fileName);
        logger.info("📁 Starting async {} upload for task: {}", fileType, taskId);

        try {
            // Update task to RUNNING status
            task.setStatus("RUNNING");
            task.setFileName(fileName);
            task.updateProgress(0.0, "INITIALIZING", "Starting " + fileType.toLowerCase() + " file upload...");
            taskDao.update(task);

            // Check for cancellation
            if (checkCancellation(task)) {
                return CompletableFuture.completedFuture(task);
            }

            // Phase 1: File Validation (0% → 20%)
            task.updateProgress(10.0, "VALIDATING", "Validating file format...");
            taskDao.update(task);
            System.out.println("🔍 SYSTEM.OUT: Progress 10% - Validating file format...");
            Thread.sleep(2000); // 2 second delay to see progress

            if (fileContent == null || fileContent.length == 0) {
                failTask(task, "File is empty");
                return CompletableFuture.completedFuture(task);
            }

            // Phase 2: File Parsing (20% → 50%)
            task.updateProgress(20.0, "PARSING", "Parsing TSV file...");
            taskDao.update(task);
            System.out.println("📊 SYSTEM.OUT: Progress 20% - Parsing TSV file...");
            Thread.sleep(2000); // 2 second delay to see progress

            // Create a mock MultipartFile from byte array
            MultipartFile mockFile = createMockMultipartFile(fileContent, fileName);
            ArrayList<HashMap<String, String>> tsvData = ProcessTsv.processTsv(mockFile, headers);
            
            task.updateProgress(40.0, "PARSING", String.format("Parsed %d records from file", tsvData.size()));
            task.setTotalRecords(tsvData.size());
            taskDao.update(task);
            System.out.println("📊 SYSTEM.OUT: Progress 40% - Parsed " + tsvData.size() + " records");
            Thread.sleep(2000); // 2 second delay to see progress

            // Check for cancellation
            if (checkCancellation(task)) {
                return CompletableFuture.completedFuture(task);
            }

            // Phase 3: Data Processing (50% → 90%)
            task.updateProgress(50.0, "PROCESSING", "Processing and validating data...");
            taskDao.update(task);
            System.out.println("⚙️ SYSTEM.OUT: Progress 50% - Processing and validating data...");
            Thread.sleep(3000); // 3 second delay to see progress

            UploadResponse result = processor.process(tsvData);

            task.updateProgress(80.0, "PROCESSING", "Saving data to database...");
            taskDao.update(task);
            System.out.println("💾 SYSTEM.OUT: Progress 80% - Saving data to database...");
            Thread.sleep(2000); // 2 second delay to see progress

            // Phase 4: Completion (90% → 100%)
            if (result.isSuccess()) {
                completeTask(task, result, fileType);
                System.out.println("✅ SYSTEM.OUT: Progress 100% - " + fileType + " upload completed successfully");
            } else {
                failTask(task, "Upload failed: " + result.getMessage() + 
                              (result.getErrors() != null ? ". Errors: " + String.join(", ", result.getErrors()) : ""));
            }

            logger.info("✅ {} upload completed for task: {}", fileType, taskId);

        } catch (Exception e) {
            logger.error("❌ {} upload failed for task {}: {}", fileType, taskId, e.getMessage(), e);
            failTask(task, fileType + " upload failed: " + e.getMessage());
        }

        return CompletableFuture.completedFuture(task);
    }

    /**
     * Check if task cancellation was requested
     */
    private boolean checkCancellation(Task task) {
        Task refreshedTask = taskDao.select(task.getId());
        if (refreshedTask != null && refreshedTask.isCancellationRequested()) {
            logger.info("🛑 Cancellation detected for task: {}", task.getId());
            task.setStatus("CANCELLED");
            task.updateProgress(task.getProgressPercentage(), "CANCELLED", "Upload was cancelled by user");
            taskDao.update(task);
            return true;
        }
        return false;
    }

    /**
     * Complete task with success status
     */
    private void completeTask(Task task, UploadResponse result, String fileType) {
        task.setStatus("COMPLETED");
        task.setEndTime(new java.util.Date());
        task.setProcessedRecords(result.getRecordCount());
        task.setErrorCount(result.getErrorCount());
        task.updateProgress(100.0, "COMPLETED", 
                          String.format("%s upload completed: %d processed, %d errors", 
                                      fileType, result.getRecordCount(), result.getErrorCount()));
        
        // Add result summary to parameters
        String summary = String.format("Success: %d, Errors: %d", result.getRecordCount(), result.getErrorCount());
        task.setParameters(task.getParameters() + ", Results: " + summary);
        
        taskDao.update(task);
        logger.info("✅ Task {} completed successfully", task.getId());
    }

    /**
     * Mark task as failed with error message
     */
    private void failTask(Task task, String errorMessage) {
        task.setStatus("FAILED");
        task.setEndTime(new java.util.Date());
        task.setErrorMessage(errorMessage);
        task.updateProgress(task.getProgressPercentage(), "FAILED", errorMessage);
        taskDao.update(task);
        logger.error("❌ Task {} failed: {}", task.getId(), errorMessage);
    }

    /**
     * Create a mock MultipartFile from byte array for async processing
     */
    private MultipartFile createMockMultipartFile(byte[] content, String fileName) {
        return new MultipartFile() {
            @Override
            public String getName() { return "file"; }
            
            @Override
            public String getOriginalFilename() { return fileName; }
            
            @Override
            public String getContentType() { return "text/tab-separated-values"; }
            
            @Override
            public boolean isEmpty() { return content == null || content.length == 0; }
            
            @Override
            public long getSize() { return content != null ? content.length : 0; }
            
            @Override
            public byte[] getBytes() { return content; }
            
            @Override
            public java.io.InputStream getInputStream() {
                return new java.io.ByteArrayInputStream(content);
            }
            
            @Override
            public void transferTo(java.io.File dest) throws java.io.IOException, IllegalStateException {
                java.nio.file.Files.write(dest.toPath(), content);
            }
        };
    }

    /**
     * Functional interface for file processing
     */
    @FunctionalInterface
    private interface FileProcessor {
        UploadResponse process(ArrayList<HashMap<String, String>> data) throws ApiException;
    }
}
