package com.iris.increff.controller;

import com.iris.increff.dao.TaskDao;
import com.iris.increff.model.Task;
import com.iris.increff.service.AsyncUploadService;
import com.iris.increff.service.StyleService;
import com.iris.increff.service.StoreService;
import com.iris.increff.service.SkuService;
import com.iris.increff.service.SalesService;
import com.iris.increff.util.ProcessTsv;
import com.iris.increff.util.ApiException;
import io.swagger.annotations.Api;
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

    @Autowired
    private StyleService styleService;
    
    @Autowired
    private StoreService storeService;
    
    @Autowired
    private SkuService skuService;
    
    @Autowired
    private SalesService salesService;

    @Autowired
    private AsyncUploadService asyncUploadService;

    @Autowired
    private TaskDao taskDao;

    @ApiOperation(value = "Upload Styles TSV")
    @RequestMapping(value = "/api/file/upload/styles", method = RequestMethod.POST)
    public ResponseEntity<?> uploadStylesTsv(@RequestPart("file") MultipartFile file) {
        try {
            // Parse TSV using existing utility
            ArrayList<HashMap<String, String>> tsvData = ProcessTsv.processTsv(file, ProcessTsv.stylesHeaders);
            
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
            ArrayList<HashMap<String, String>> tsvData = ProcessTsv.processTsv(file, ProcessTsv.storeHeaders);
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
            ArrayList<HashMap<String, String>> tsvData = ProcessTsv.processTsv(file, ProcessTsv.skuHeaders);
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
            ArrayList<HashMap<String, String>> tsvData = ProcessTsv.processTsv(file, ProcessTsv.salesHeaders);
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
        System.out.println("Export Input File Download is Successful :- " + fileName);
    }

    @ApiOperation(value = "Download template for File")
    @RequestMapping(path = "/api/file/template/{fileName}", method = RequestMethod.GET)
    public void exportTemplateTSV(@PathVariable String fileName, HttpServletResponse response) throws IOException {
        ProcessTsv.createFileResponse(new File("src/main/resources/Files/fileTemplate.tsv"), response);
        System.out.println("Download Input Template File is Successful :- " + fileName);

    }

    @ApiOperation(value = "Download Errors for input")
    @RequestMapping(path = "/api/file/errors/{fileName}", method = RequestMethod.GET)
    public void exportErrorTSV(@PathVariable String fileName, HttpServletResponse response) throws IOException {
        ProcessTsv.createFileResponse(new File("src/main/resources/Files/fileError.tsv"), response);
        System.out.println("Download Validation Error for File is Successful :- " + fileName);

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

    @ApiOperation(value = "Download Styles data as TSV")
    @RequestMapping(path = "/api/file/download/styles", method = RequestMethod.GET)
    public void downloadStylesData(HttpServletResponse response) throws IOException {
        List<com.iris.increff.model.Style> styles = styleService.getAllStyles();
        ProcessTsv.createStylesDataResponse(styles, response);
    }

    @ApiOperation(value = "Download Stores data as TSV")
    @RequestMapping(path = "/api/file/download/stores", method = RequestMethod.GET)
    public void downloadStoresData(HttpServletResponse response) throws IOException {
        List<com.iris.increff.model.Store> stores = storeService.getAllStores();
        ProcessTsv.createStoresDataResponse(stores, response);
    }

    @ApiOperation(value = "Download SKUs data as TSV")
    @RequestMapping(path = "/api/file/download/skus", method = RequestMethod.GET)
    public void downloadSkusData(HttpServletResponse response) throws IOException {
        List<com.iris.increff.model.SKU> skus = skuService.getAllSKUs();
        ProcessTsv.createSkusDataResponse(skus, response);
    }

    @ApiOperation(value = "Download Sales data as TSV")
    @RequestMapping(path = "/api/file/download/sales", method = RequestMethod.GET)
    public void downloadSalesData(HttpServletResponse response) throws IOException {
        List<com.iris.increff.model.Sales> sales = salesService.getAllSales();
        ProcessTsv.createSalesDataResponse(sales, response);
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
        System.out.println("📁 SYSTEM.OUT: Async Styles upload requested: " + file.getOriginalFilename());
        return processAsyncUpload(file, "STYLES_UPLOAD", 
                                (taskId, fileContent, fileName) -> asyncUploadService.uploadStylesAsync(taskId, fileContent, fileName));
    }

    @ApiOperation(value = "Upload Stores TSV (Async)")
    @RequestMapping(value = "/api/file/upload/stores/async", method = RequestMethod.POST)
    @Transactional
    public ResponseEntity<Task> uploadStoresTsvAsync(@RequestPart("file") MultipartFile file) {
        System.out.println("📁 SYSTEM.OUT: Async Stores upload requested: " + file.getOriginalFilename());
        return processAsyncUpload(file, "STORES_UPLOAD",
                                (taskId, fileContent, fileName) -> asyncUploadService.uploadStoresAsync(taskId, fileContent, fileName));
    }

    @ApiOperation(value = "Upload SKUs TSV (Async)")
    @RequestMapping(value = "/api/file/upload/skus/async", method = RequestMethod.POST)
    @Transactional
    public ResponseEntity<Task> uploadSkusTsvAsync(@RequestPart("file") MultipartFile file) {
        System.out.println("📁 SYSTEM.OUT: Async SKUs upload requested: " + file.getOriginalFilename());
        return processAsyncUpload(file, "SKUS_UPLOAD",
                                (taskId, fileContent, fileName) -> asyncUploadService.uploadSkusAsync(taskId, fileContent, fileName));
    }

    @ApiOperation(value = "Upload Sales TSV (Async)")
    @RequestMapping(value = "/api/file/upload/sales/async", method = RequestMethod.POST)
    @Transactional
    public ResponseEntity<Task> uploadSalesTsvAsync(@RequestPart("file") MultipartFile file) {
        System.out.println("📁 SYSTEM.OUT: Async Sales upload requested: " + file.getOriginalFilename());
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
                System.out.println("✅ SYSTEM.OUT: Async upload started with task ID: " + task.getId());
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
            System.out.println("❌ SYSTEM.OUT: Failed to start async upload: " + e.getMessage());
            
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

    /**
     * Functional interface for async processing
     */
    @FunctionalInterface
    private interface AsyncProcessor {
        void process(Long taskId, byte[] fileContent, String fileName);
    }

}
