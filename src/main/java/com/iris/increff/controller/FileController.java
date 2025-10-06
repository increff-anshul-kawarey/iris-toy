package com.iris.increff.controller;

import com.iris.increff.dao.TaskDao;
import com.iris.increff.model.Task;
import com.iris.increff.service.AsyncUploadService;
import com.iris.increff.service.AsyncDownloadService;
import com.iris.increff.service.StyleService;
import com.iris.increff.service.StoreService;
import com.iris.increff.service.SkuService;
import com.iris.increff.service.SalesService;
import com.iris.increff.service.FileProcessingService;
import com.iris.increff.config.TsvProperties;
import com.iris.increff.util.ProcessTsv;
import com.iris.increff.exception.ApiException;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


@Api
@RestController
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private StyleService styleService;
    
    @Autowired
    private StoreService storeService;
    
    @Autowired
    private SkuService skuService;
    
    @Autowired
    private SalesService salesService;

    @Autowired
    private FileProcessingService fileProcessingService;

    @Autowired
    private AsyncUploadService asyncUploadService;
    @Autowired
    private TsvProperties tsvProperties;

    @Autowired
    private AsyncDownloadService asyncDownloadService;

    @Autowired
    private TaskDao taskDao;

    @Autowired
    private com.iris.increff.service.TaskService taskService;

    @Autowired
    private com.iris.increff.service.DataClearingService dataClearingService;

    @javax.persistence.PersistenceContext
    private javax.persistence.EntityManager entityManager;

    @ApiOperation(value = "Upload Styles TSV")
    @RequestMapping(value = "/api/file/upload/styles", method = RequestMethod.POST)
    public ResponseEntity<?> uploadStylesTsv(@RequestPart("file") MultipartFile file) {
        try {
            // Parse TSV using existing utility
            ArrayList<HashMap<String, String>> tsvData = fileProcessingService.processTsv(file, tsvProperties.getStylesHeaders());
            
            // Process and save via service
            UploadResponse result = styleService.processAndSaveStyles(tsvData);

            if (result.isSuccess()) {
                result.setMessage("Styles uploaded successfully");
                return ResponseEntity.ok(result);
            } else {
                result.setMessage("Styles upload failed");
                return ResponseEntity.badRequest().body(result);
            }
            
        } catch (ApiException e) {
            List<String> errors = new ArrayList<>();
            errors.add(e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse("Styles upload failed", errors));
        } catch (Exception e) {
            List<String> errors = new ArrayList<>();
            errors.add("Unexpected error: " + e.getMessage());
            return ResponseEntity.status(500).body(createErrorResponse("Styles upload failed", errors));
        }
    }

    @ApiOperation(value = "Upload Stores TSV")
    @RequestMapping(value = "/api/file/upload/stores", method = RequestMethod.POST)
    public ResponseEntity<?> uploadStoresTsv(@RequestPart("file") MultipartFile file) {
        try {
            ArrayList<HashMap<String, String>> tsvData = fileProcessingService.processTsv(file, tsvProperties.getStoreHeaders());
            UploadResponse result = storeService.processAndSaveStores(tsvData);

            if (result.isSuccess()) {
                result.setMessage("Stores uploaded successfully");
                return ResponseEntity.ok(result);
            } else {
                result.setMessage("Stores upload failed");
                return ResponseEntity.badRequest().body(result);
            }
            
        } catch (ApiException e) {
            List<String> errors = new ArrayList<>();
            errors.add(e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse("Stores upload failed", errors));
        } catch (Exception e) {
            List<String> errors = new ArrayList<>();
            errors.add("Unexpected error: " + e.getMessage());
            return ResponseEntity.status(500).body(createErrorResponse("Stores upload failed", errors));
        }
    }

    @ApiOperation(value = "Upload SKUs TSV")
    @RequestMapping(value = "/api/file/upload/skus", method = RequestMethod.POST)
    public ResponseEntity<?> uploadSkusTsv(@RequestPart("file") MultipartFile file) {
        try {
            ArrayList<HashMap<String, String>> tsvData = fileProcessingService.processTsv(file, tsvProperties.getSkuHeaders());
            UploadResponse result = skuService.processAndSaveSKUs(tsvData);

            if (result.isSuccess()) {
                result.setMessage("SKUs uploaded successfully");
                return ResponseEntity.ok(result);
            } else {
                result.setMessage("SKUs upload failed");
                return ResponseEntity.badRequest().body(result);
            }
            
        } catch (ApiException e) {
            List<String> errors = new ArrayList<>();
            errors.add(e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse("SKUs upload failed", errors));
        } catch (Exception e) {
            List<String> errors = new ArrayList<>();
            errors.add("Unexpected error: " + e.getMessage());
            return ResponseEntity.status(500).body(createErrorResponse("SKUs upload failed", errors));
        }
    }

    @ApiOperation(value = "Upload Sales TSV")
    @RequestMapping(value = "/api/file/upload/sales", method = RequestMethod.POST)
    public ResponseEntity<?> uploadSalesTsv(@RequestPart("file") MultipartFile file) {
        try {
            ArrayList<HashMap<String, String>> tsvData = fileProcessingService.processTsv(file, tsvProperties.getSalesHeaders());
            UploadResponse result = salesService.processAndSaveSales(tsvData);

            if (result.isSuccess()) {
                result.setMessage("Sales uploaded successfully");
                return ResponseEntity.ok(result);
            } else {
                result.setMessage("Sales upload failed");
                return ResponseEntity.badRequest().body(result);
            }
            
        } catch (ApiException e) {
            List<String> errors = new ArrayList<>();
            errors.add(e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse("Sales upload failed", errors));
        } catch (Exception e) {
            List<String> errors = new ArrayList<>();
            errors.add("Unexpected error: " + e.getMessage());
            return ResponseEntity.status(500).body(createErrorResponse("Sales upload failed", errors));
        }
    }

    /**
     * Create a structured error response for upload failures
     *
     * @param message Main error message
     * @param errors List of specific errors
     * @return Structured error response
     */
    private HashMap<String, Object> createErrorResponse(String message, List<String> errors) {
        HashMap<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("errors", errors);
        response.put("errorCount", errors.size());
        return response;
    }

    /**
     * Create a detailed error response with messages and errors
     */
    private HashMap<String, Object> createDetailedErrorResponse(String message, List<String> errors, List<String> messages) {
        HashMap<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("errors", errors);
        response.put("messages", messages);
        response.put("errorCount", errors.size());
        return response;
    }

    /**
     * Create a success response with messages and record count
     */
    private HashMap<String, Object> createSuccessResponse(String message, List<String> messages, Integer recordCount) {
        HashMap<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("messages", messages);
        response.put("recordCount", recordCount);
        return response;
    }

    @ApiOperation(value = "Download Input for File ")
    @RequestMapping(path = "/api/file/input/{fileName}", method = RequestMethod.GET)
    public void exportInputTSV(@PathVariable String fileName, HttpServletResponse response) throws IOException, InterruptedException {
        ProcessTsv.createFileResponse(new File("src/main/resources/Files/fileInput.tsv"), response);
        logger.info("Input file template downloaded successfully: {}", fileName);
    }

    @ApiOperation(value = "Download template for File")
    @RequestMapping(path = "/api/file/template/{fileName}", method = RequestMethod.GET)
    public void exportTemplateTSV(@PathVariable String fileName, HttpServletResponse response) throws IOException {
        ProcessTsv.createFileResponse(new File("src/main/resources/Files/fileTemplate.tsv"), response);
        logger.info("Input template downloaded successfully: {}", fileName);

    }

    @ApiOperation(value = "Download Errors for input")
    @RequestMapping(path = "/api/file/errors/{fileName}", method = RequestMethod.GET)
    public void exportErrorTSV(@PathVariable String fileName, HttpServletResponse response) throws IOException {
        ProcessTsv.createFileResponse(new File("src/main/resources/Files/fileError.tsv"), response);
        logger.info("Validation errors file downloaded successfully: {}", fileName);

    }

    @ApiOperation(value = "Get data status for all file types")
    @RequestMapping(path = "/api/file/status", method = RequestMethod.GET)
    public ResponseEntity<?> getDataStatus() {
        HashMap<String, Object> status = new HashMap<>();

        try {
            // Get counts for each data type
            status.put("styles", createDataStatus(styleService.getAllStyles().size()));
            status.put("stores", createDataStatus(storeService.getAllStores().size()));
            status.put("skus", createDataStatus(skuService.getAllSKUs().size()));
            status.put("sales", createDataStatus(salesService.getSalesCount()));

            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error getting data status: " + e.getMessage());
        }
    }

    /**
     * Clear all data in the database.
     * This provides an explicit "fresh start" operation for testing/development scenarios.
     * Respects foreign key constraints by clearing in proper dependency order.
     * 
     * WARNING: This is a destructive operation that cannot be undone.
     * Should be used with caution and only when you want to completely reset the database.
     */
    @ApiOperation(value = "Clear all data from database (fresh start)")
    @RequestMapping(path = "/api/data/clear-all", method = RequestMethod.DELETE)
    @Transactional
    public ResponseEntity<?> clearAllData() {
        try {
            // Get counts before clearing (for response message)
            long salesCount = salesService.getSalesCount();
            int skuCount = skuService.getAllSKUs().size();
            int styleCount = styleService.getAllStyles().size();
            int storeCount = storeService.getAllStores().size();
            
            // Clear all data in proper dependency order
            dataClearingService.clearAllData();
            
            // Build response message
            HashMap<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "All data cleared successfully");
            response.put("deletedRecords", createDeletedRecordsMap(salesCount, skuCount, styleCount, storeCount));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            HashMap<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error clearing data: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Helper method to create a map of deleted record counts
     */
    private HashMap<String, Object> createDeletedRecordsMap(long sales, int skus, int styles, int stores) {
        HashMap<String, Object> deleted = new HashMap<>();
        deleted.put("sales", sales);
        deleted.put("skus", skus);
        deleted.put("styles", styles);
        deleted.put("stores", stores);
        deleted.put("total", sales + skus + styles + stores);
        return deleted;
    }

    @ApiOperation(value = "Download Styles data as TSV")
    @RequestMapping(path = "/api/file/download/styles", method = RequestMethod.GET)
    public void downloadStylesData(HttpServletResponse response) throws IOException {
        List<com.iris.increff.model.Style> styles = styleService.getAllStyles();
        ProcessTsv.createStylesDataResponse(styles, response, tsvProperties.getStylesHeaders());
    }

    @ApiOperation(value = "Download Stores data as TSV")
    @RequestMapping(path = "/api/file/download/stores", method = RequestMethod.GET)
    public void downloadStoresData(HttpServletResponse response) throws IOException {
        List<com.iris.increff.model.Store> stores = storeService.getAllStores();
        ProcessTsv.createStoresDataResponse(stores, response, tsvProperties.getStoreHeaders());
    }

    @ApiOperation(value = "Download SKUs data as TSV")
    @RequestMapping(path = "/api/file/download/skus", method = RequestMethod.GET)
    public void downloadSkusData(HttpServletResponse response) throws IOException {
        List<com.iris.increff.model.SKU> skus = skuService.getAllSKUs();
        ProcessTsv.createSkusDataResponse(skus, response, tsvProperties.getSkuHeaders());
    }

    @ApiOperation(value = "Download Sales data as TSV")
    @RequestMapping(path = "/api/file/download/sales", method = RequestMethod.GET)
    public void downloadSalesData(HttpServletResponse response) throws IOException {
        List<com.iris.increff.model.Sales> sales = salesService.getAllSales();
        ProcessTsv.createSalesDataResponse(sales, response, tsvProperties.getSalesHeaders());
    }

    /**
     * Create a data status object with count and existence info
     */
    private HashMap<String, Object> createDataStatus(long count) {
        HashMap<String, Object> status = new HashMap<>();
        status.put("count", count);
        status.put("exists", count > 0);
        return status;
    }

    // ==================== ASYNC UPLOAD ENDPOINTS ====================

    @ApiOperation(value = "Upload Styles TSV (Async)")
    @RequestMapping(value = "/api/file/upload/styles/async", method = RequestMethod.POST)
    @Transactional
    public ResponseEntity<Task> uploadStylesTsvAsync(@RequestPart("file") MultipartFile file) {
        logger.info("Async Styles upload requested: {}", file.getOriginalFilename());
        return processAsyncUpload(file, "STYLES_UPLOAD", 
                                (taskId, fileContent, fileName) -> asyncUploadService.uploadStylesAsync(taskId, fileContent, fileName));
    }

    @ApiOperation(value = "Upload Stores TSV (Async)")
    @RequestMapping(value = "/api/file/upload/stores/async", method = RequestMethod.POST)
    @Transactional
    public ResponseEntity<Task> uploadStoresTsvAsync(@RequestPart("file") MultipartFile file) {
        logger.info("Async Stores upload requested: {}", file.getOriginalFilename());
        return processAsyncUpload(file, "STORES_UPLOAD",
                                (taskId, fileContent, fileName) -> asyncUploadService.uploadStoresAsync(taskId, fileContent, fileName));
    }

    @ApiOperation(value = "Upload SKUs TSV (Async)")
    @RequestMapping(value = "/api/file/upload/skus/async", method = RequestMethod.POST)
    @Transactional
    public ResponseEntity<Task> uploadSkusTsvAsync(@RequestPart("file") MultipartFile file) {
        logger.info("Async SKUs upload requested: {}", file.getOriginalFilename());
        return processAsyncUpload(file, "SKUS_UPLOAD",
                                (taskId, fileContent, fileName) -> asyncUploadService.uploadSkusAsync(taskId, fileContent, fileName));
    }

    @ApiOperation(value = "Upload Sales TSV (Async)")
    @RequestMapping(value = "/api/file/upload/sales/async", method = RequestMethod.POST)
    @Transactional
    public ResponseEntity<Task> uploadSalesTsvAsync(@RequestPart("file") MultipartFile file) {
        logger.info("Async Sales upload requested: {}", file.getOriginalFilename());
        return processAsyncUpload(file, "SALES_UPLOAD",
                                (taskId, fileContent, fileName) -> asyncUploadService.uploadSalesAsync(taskId, fileContent, fileName));
    }

    /**
     * Generic async upload processing
     * 
     * @param file Uploaded file
     * @param taskType Type of upload task
     * @param processor Async processor function
     * @return HTTP 202 with task details
     */
    private ResponseEntity<Task> processAsyncUpload(MultipartFile file, String taskType, AsyncProcessor processor) {
        try {
            // Read file content immediately (in controller thread)
            byte[] fileContent = file.getBytes();
            String fileName = file.getOriginalFilename();
            
            // Create task immediately
            Task task = new Task();
            task.setTaskType(taskType);
            task.setStatus("PENDING");
            task.setStartTime(new java.util.Date());
            task.setUserId("system");
            task.setFileName(fileName);
            task.setParameters("fileName=" + fileName + ", fileSize=" + fileContent.length);
            task.updateProgress(0.0, "PENDING", "Upload task created, waiting to start...");
            
            // Save task to get ID
            taskDao.insert(task);
            
            // Start async processing with pre-read content
            try {
                processor.process(task.getId(), fileContent, fileName);
                logger.info("Async upload started with task ID: {}", task.getId());
                return ResponseEntity.accepted().body(task); // HTTP 202 Accepted
            } catch (RuntimeException e) {
                // Handle thread pool rejection
                if (e.getMessage().contains("Thread pool queue is full")) {
                    task.setStatus("FAILED");
                    task.setErrorMessage("System is busy. Too many concurrent uploads. Please try again later.");
                    taskDao.update(task);
                    return ResponseEntity.status(429).body(task); // HTTP 429 Too Many Requests
                }
                throw e;
            }
            
        } catch (Exception e) {
            logger.error("Failed to start async upload: {}", e.getMessage(), e);
            
            // Create error response with details
            Task errorTask = new Task();
            errorTask.setTaskType(taskType);
            errorTask.setStatus("FAILED");
            errorTask.setErrorMessage("Failed to start async upload: " + e.getMessage());
            errorTask.setUserId("system");
            errorTask.setFileName(file.getOriginalFilename());
            errorTask.setStartTime(new java.util.Date());
            errorTask.setEndTime(new java.util.Date());
            
            return ResponseEntity.status(500).body(errorTask);
        }
    }

    // ==================== ASYNC DOWNLOAD ENDPOINTS ====================

    @ApiOperation(value = "Download Styles TSV (Async)")
    @RequestMapping(value = "/api/file/download/styles/async", method = RequestMethod.POST)
    public ResponseEntity<Task> downloadStylesAsync() {
        logger.info("Creating download task for Styles");
        Task task = createDownloadTask("STYLES_DOWNLOAD");
        
        // Persist task in a NEW transaction that commits immediately
        task = taskService.createTaskInNewTransaction(task);
        logger.info("Task created and committed with ID: {}", task.getId());
        
        // Now call async service - task is already visible in database
        asyncDownloadService.downloadStylesAsync(task.getId());
        logger.info("Async service called, returning task");
        
        return ResponseEntity.accepted().body(task);
    }

    @ApiOperation(value = "Download Stores TSV (Async)")
    @RequestMapping(value = "/api/file/download/stores/async", method = RequestMethod.POST)
    public ResponseEntity<Task> downloadStoresAsync() {
        Task task = createDownloadTask("STORES_DOWNLOAD");
        task = taskService.createTaskInNewTransaction(task);
        asyncDownloadService.downloadStoresAsync(task.getId());
        return ResponseEntity.accepted().body(task);
    }

    @ApiOperation(value = "Download SKUs TSV (Async)")
    @RequestMapping(value = "/api/file/download/skus/async", method = RequestMethod.POST)
    public ResponseEntity<Task> downloadSkusAsync() {
        Task task = createDownloadTask("SKUS_DOWNLOAD");
        task = taskService.createTaskInNewTransaction(task);
        asyncDownloadService.downloadSkusAsync(task.getId());
        return ResponseEntity.accepted().body(task);
    }

    @ApiOperation(value = "Download Sales TSV (Async)")
    @RequestMapping(value = "/api/file/download/sales/async", method = RequestMethod.POST)
    public ResponseEntity<Task> downloadSalesAsync() {
        Task task = createDownloadTask("SALES_DOWNLOAD");
        task = taskService.createTaskInNewTransaction(task);
        asyncDownloadService.downloadSalesAsync(task.getId());
        return ResponseEntity.accepted().body(task);
    }

    @ApiOperation(value = "Download NOOS Results TSV (Async)")
    @RequestMapping(value = "/api/file/download/noos/async", method = RequestMethod.POST)
    public ResponseEntity<Task> downloadNoosAsync(@RequestParam(required = false) Long runId) {
        Task task = createDownloadTask("NOOS_DOWNLOAD");
        task = taskService.createTaskInNewTransaction(task);
        asyncDownloadService.downloadNoosResultsAsync(task.getId(), runId);
        return ResponseEntity.accepted().body(task);
    }

    @ApiOperation(value = "Stream async task result TSV")
    @RequestMapping(value = "/api/tasks/{taskId}/result", method = RequestMethod.GET)
    public ResponseEntity<?> streamTaskResult(@PathVariable Long taskId, HttpServletResponse response) {
        Task task = taskDao.select(taskId);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        if (!"COMPLETED".equals(task.getStatus()) || task.getResultUrl() == null) {
            return ResponseEntity.status(409).body("Task not completed yet");
        }
        try {
            File file = new File(task.getResultUrl());
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }
            ProcessTsv.createFileResponse(file, response);
            return null; // Response handled via HttpServletResponse
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to stream result: " + e.getMessage());
        }
    }

    /**
     * Create download task object (does not persist)
     * Caller is responsible for persisting the task
     */
    private Task createDownloadTask(String taskType) {
        Task task = new Task();
        task.setTaskType(taskType);
        task.setStatus("PENDING");
        task.setStartTime(new java.util.Date());
        task.setUserId("system");
        task.updateProgress(0.0, "PENDING", "Download task created, waiting to start...");
        return task;
    }

    /**
     * Functional interface for async processing
     */
    @FunctionalInterface
    private interface AsyncProcessor {
        void process(Long taskId, byte[] fileContent, String fileName);
    }

}
