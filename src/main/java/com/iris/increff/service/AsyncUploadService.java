package com.iris.increff.service;

import com.iris.increff.controller.UploadResponse;
import com.iris.increff.dao.TaskDao;
import com.iris.increff.model.Task;
import com.iris.increff.exception.ApiException;
import com.iris.increff.config.TsvProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;

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
    private TsvProperties tsvProperties;
    
    @Autowired
    private FileProcessingService fileProcessingService;

    //DAOS
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
     */
    @Async("fileExecutor")
    @Transactional
    public void uploadStylesAsync(Long taskId, byte[] fileContent, String fileName) {
        processFileAsync(taskId, fileContent, fileName, "STYLES", tsvProperties.getStylesHeaders(), 
                               (data) -> styleService.processAndSaveStyles(data));
    }

    /**
     * Async Stores Upload with Progress Tracking
     * 
     * @param taskId Task ID for progress tracking
     * @param fileContent Pre-read file content as byte array
     * @param fileName Original file name
     */
    @Async("fileExecutor")
    @Transactional
    public void uploadStoresAsync(Long taskId, byte[] fileContent, String fileName) {
        processFileAsync(taskId, fileContent, fileName, "STORES", tsvProperties.getStoreHeaders(),
                               (data) -> storeService.processAndSaveStores(data));
    }

    /**
     * Async SKUs Upload with Progress Tracking
     * 
     * @param taskId Task ID for progress tracking
     * @param fileContent Pre-read file content as byte array
     * @param fileName Original file name
     */
    @Async("fileExecutor")
    @Transactional
    public void uploadSkusAsync(Long taskId, byte[] fileContent, String fileName) {
        processFileAsync(taskId, fileContent, fileName, "SKUS", tsvProperties.getSkuHeaders(),
                               (data) -> skuService.processAndSaveSKUs(data));
    }

    /**
     * Async Sales Upload with Progress Tracking
     * 
     * @param taskId Task ID for progress tracking
     * @param fileContent Pre-read file content as byte array
     * @param fileName Original file name
     */
    @Async("fileExecutor")
    @Transactional
    public void uploadSalesAsync(Long taskId, byte[] fileContent, String fileName) {
        processFileAsync(taskId, fileContent, fileName, "SALES", tsvProperties.getSalesHeaders(),
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
     */
    private void processFileAsync(Long taskId, byte[] fileContent, String fileName, 
                                                     String fileType, String[] headers,
                                                     Function<ArrayList<HashMap<String, String>>, UploadResponse> processor) {
        Task task = taskDao.select(taskId);
        if (task == null) {
            logger.error("❌ Task not found: {}", taskId);
            return;
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
                return;
            }

            // Phase 1: File Validation (0% → 20%)
            task.updateProgress(10.0, "VALIDATING", "Validating file format...");
            taskDao.update(task);
            System.out.println("🔍 SYSTEM.OUT: Progress 10% - Validating file format...");

            if (fileContent == null || fileContent.length == 0) {
                failTask(task, "File is empty");
                return;
            }

            // Phase 2: File Parsing (20% → 50%)
            task.updateProgress(20.0, "PARSING", "Parsing TSV file...");
            taskDao.update(task);
            System.out.println("📊 SYSTEM.OUT: Progress 20% - Parsing TSV file...");

            ArrayList<HashMap<String, String>> tsvData = fileProcessingService.processTsv(fileContent, fileName, headers);

            task.updateProgress(40.0, "IN_PROGRESS", "TSV parsed, processing " + tsvData.size() + " rows...");
            taskDao.update(task);
            System.out.println("📊 SYSTEM.OUT: Progress 40% - Parsed " + tsvData.size() + " records");

            // Check for cancellation
            if (checkCancellation(task)) {
                return;
            }

            // Phase 3: Data Processing (50% → 90%)
            task.updateProgress(50.0, "PROCESSING", "Processing and validating data...");
            taskDao.update(task);
            System.out.println("⚙️ SYSTEM.OUT: Progress 50% - Processing and validating data...");

            UploadResponse result = processor.apply(tsvData);

            task.updateProgress(80.0, "PROCESSING", "Saving data to database...");
            taskDao.update(task);
            System.out.println("💾 SYSTEM.OUT: Progress 80% - Saving data to database...");

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
     * Functional interface for file processing
     */
    @FunctionalInterface
    private interface FileProcessor {
        UploadResponse process(ArrayList<HashMap<String, String>> data) throws ApiException;
    }
}
