package com.iris.increff.service;

import com.iris.increff.controller.UploadResponse;
import com.iris.increff.dao.SalesDao;
import com.iris.increff.model.Sales;
import com.iris.increff.model.SKU;
import com.iris.increff.model.Store;
import com.iris.increff.exception.ApiException;
import com.iris.increff.service.ValidationService.ValidationResult;
import com.iris.increff.service.ErrorTrackingService.ErrorTracker;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for handling Sales-related operations including TSV processing.
 * Handles complex field mapping, validation, normalization, and database operations.
 * Depends on SkuService and StoreService for entity lookups.
 * 
 * @author Your Name
 * @version 1.0
 * @since 2025-01-01
 */
@Service
public class SalesService {

    private static final Logger logger = LoggerFactory.getLogger(SalesService.class);

    @Autowired
    private SalesDao salesDao;

    @Autowired
    private SkuService skuService;

    @Autowired
    private StoreService storeService;

    @Autowired
    private DataClearingService dataClearingService;

    @Autowired
    private AuditService auditService;
    
    @Autowired
    private ValidationService validationService;
    
    @Autowired
    private ErrorTrackingService errorTrackingService;
    /**
     * Expected date format in sales TSV files
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat(DATE_FORMAT);

    /**
     * Process and save sales from TSV data with enhanced validation and error tracking.
     * Expected TSV format: day, sku, channel, quantity, discount, revenue
     * Maps to entity fields: date, skuId (via lookup), storeId (via lookup), quantity, discount, revenue
     * 
     * @param tsvData Parsed TSV data as list of row maps
     * @return UploadResponse with success status, messages, and error tracking information
     */
    @Transactional
    public UploadResponse processAndSaveSales(ArrayList<HashMap<String, String>> tsvData) {
        UploadResponse response = new UploadResponse();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<String> messages = new ArrayList<>();
        List<Sales> salesToSave = new ArrayList<>();
        
        // Initialize error tracker
        String[] headers = {"day", "sku", "channel", "quantity", "discount", "revenue"};
        ErrorTracker errorTracker = new ErrorTracker(headers);

        // First pass: Comprehensive validation with detailed error tracking
        for (int i = 0; i < tsvData.size(); i++) {
            HashMap<String, String> row = tsvData.get(i);
            int rowNumber = i + 2; // +2 because: +1 for 0-indexing, +1 for header row
            
            try {
                Sales sales = convertTsvRowToSalesWithValidation(row, rowNumber, errorTracker);
                if (sales != null) {
                    salesToSave.add(sales);
                }
                
            } catch (Exception e) {
                errorTracker.addError(rowNumber, row, "Unexpected error: " + e.getMessage(), "SYSTEM_ERROR");
            }
        }

        // Generate error files and summary if there are errors or skipped rows
        Map<String, String> errorFiles = new HashMap<>();
        Map<String, Object> errorSummary = new HashMap<>();
        
        if (errorTracker.hasErrors()) {
            try {
                errorFiles = errorTrackingService.saveErrorFiles(errorTracker, "SALES", String.valueOf(System.currentTimeMillis()));
                errorSummary = errorTrackingService.generateErrorSummary(errorTracker);
                
                // Add summary to legacy errors list for backward compatibility
                if (errorTracker.hasValidationErrors()) {
                    errors.add("Validation errors: " + errorTracker.getValidationErrorCount());
                }
                if (errorTracker.getSkippedCount() > 0) {
                    warnings.add("Skipped rows: " + errorTracker.getSkippedCount() + " (missing dependencies)");
                }
                
                // Show first few errors in legacy format
                errors.addAll(errorTracker.getValidationErrors().stream()
                    .limit(5) // Show first 5 validation errors
                    .map(errorRow -> "Row " + errorRow.getRowNumber() + ": " + errorRow.getErrorReason())
                    .collect(java.util.stream.Collectors.toList()));
                
                if (errorTracker.getValidationErrorCount() > 5) {
                    errors.add("... and " + (errorTracker.getValidationErrorCount() - 5) + " more validation errors. Download error files for complete details.");
                }
                
            } catch (IOException e) {
                errors.add("Failed to generate error files: " + e.getMessage());
            }
        }

        // If there are validation errors (not skipped rows), don't proceed with database operations
        if (errorTracker.hasValidationErrors()) {
            response.setSuccess(false);
            response.setErrors(errors);
            response.setWarnings(warnings);
            response.setMessages(messages);
            response.setErrorCount(errorTracker.getValidationErrorCount());
            response.setSkippedCount(errorTracker.getSkippedCount());
            response.setErrorTrackingInfo(errorSummary, errorFiles);
            return response;
        }

        // Second pass: Database operations (only if no critical errors)
        try {
            // TRUNCATE: Replace all sales data (intentional for testing/sample data scenarios)
            // Unlike master data (Styles/SKUs/Stores), sales data uses complete replacement
            // This is suitable for toy project testing where each upload is a complete test dataset
            messages.add("Clearing existing data");
            messages.add("Clearing existing sales data (complete replacement mode)");
            
            // Get count before deletion for audit log
            Long previousCount = salesDao.getTotalSalesCount();
            
            dataClearingService.clearDataForSalesUpload();
            messages.add("Data clearing completed");
            
            // Audit log the bulk deletion
            if (previousCount > 0) {
                auditService.logBulkAction("Sales", "BULK_DELETE", previousCount.intValue(), 
                    "Cleared all sales before new upload", "system");
            }

            // Save new data in batch
            if (!salesToSave.isEmpty()) {
                messages.add("Saving " + salesToSave.size() + " sales records to database");
                salesDao.saveAll(salesToSave);
                messages.add("Sales upload completed successfully");
                
                // Audit log the bulk insert
                auditService.logBulkAction("Sales", "BULK_INSERT", salesToSave.size(), 
                    "Uploaded " + salesToSave.size() + " sales records", "system");

                if (errorTracker.getSkippedCount() > 0) {
                    messages.add("Note: " + errorTracker.getSkippedCount() + " rows were skipped due to missing dependencies");
                }
            }

        } catch (Exception e) {
            errors.add("Database error: " + e.getMessage());
            response.setSuccess(false);
            response.setErrors(errors);
            response.setMessages(messages);
            response.setErrorCount(errors.size());
            throw new RuntimeException("Failed to save sales to database", e);
        }

        response.setSuccess(true);
        response.setErrors(errors);
        response.setMessages(messages);
        response.setWarnings(warnings);
        response.setRecordCount(salesToSave.size());
        response.setSkippedCount(errorTracker.getSkippedCount());
        
        // Include error files even for successful uploads (for skipped rows)
        if (errorTracker.hasErrors()) {
            response.setErrorTrackingInfo(errorSummary, errorFiles);
        }
        
        return response;
    }

    /**
     * Convert a TSV row to Sales entity with complex field mapping and validation.
     * Gracefully handles missing SKUs by returning null instead of throwing exceptions.
     * 
     * TSV fields → Entity fields mapping:
     * - "day" → date (with date parsing)
     * - "sku" → skuId (via lookup in SKU table, null if missing)
     * - "channel" → storeId (via lookup in Store table using branch)
     * - "quantity" → quantity (integer parsing)
     * - "discount" → discount (decimal parsing)  
     * - "revenue" → revenue (decimal parsing, allows 0 for promotional items)
     * 
     * @param row TSV row as key-value map
     * @param warnings List to collect non-fatal warnings
     * @param rowNumber Row number for error reporting
     * @return Validated Sales entity or null if row should be skipped
     * @throws ApiException if validation fails (critical errors only)
     */
    private Sales convertTsvRowToSales(HashMap<String, String> row, List<String> warnings, int rowNumber) throws ApiException {
        Sales sales = new Sales();
        
        // Field mapping: "day" → date
        String dayStr = row.get("day");
        if (dayStr == null || dayStr.trim().isEmpty()) {
            throw new ApiException("Date (day) cannot be empty");
        }
        
        try {
            Date salesDate = dateFormatter.parse(dayStr.trim());
            sales.setDate(salesDate);
        } catch (ParseException e) {
            throw new ApiException("Invalid date format: '" + dayStr + "'. Expected format: " + DATE_FORMAT);
        }

        // Lookup mapping: "sku" → skuId (GRACEFUL HANDLING)
        String skuCode = normalizeString(row.get("sku"));
        validateNotEmpty(skuCode, "SKU code");
        
        try {
            SKU sku = skuService.findBySku(skuCode);
            sales.setSkuId(sku.getId());
        } catch (ApiException e) {
            // GRACEFUL HANDLING: Log warning and skip this row
            String warningMsg = "Row " + rowNumber + ": Skipping sale - SKU '" + skuCode + "' not found in master data";
            warnings.add(warningMsg);
            logger.warn(warningMsg);
            return null; // Skip this row gracefully
        }

        // Lookup mapping: "channel" → storeId (channel is the store branch)
        String channel = normalizeString(row.get("channel"));
        validateNotEmpty(channel, "Channel (store branch)");
        
        try {
            Store store = storeService.findByBranch(channel);
            sales.setStoreId(store.getId());
        } catch (ApiException e) {
            throw new ApiException("Store lookup failed for channel '" + channel + "': " + e.getMessage());
        }

        // Direct mapping: quantity (integer)
        String quantityStr = row.get("quantity");
        if (quantityStr == null || quantityStr.trim().isEmpty()) {
            throw new ApiException("Quantity cannot be empty");
        }
        
        try {
            Integer quantity = Integer.parseInt(quantityStr.trim());
            if (quantity <= 0) {
                throw new ApiException("Quantity must be positive, found: " + quantity);
            }
            sales.setQuantity(quantity);
        } catch (NumberFormatException e) {
            throw new ApiException("Invalid quantity format: '" + quantityStr + "'. Expected a positive integer.");
        }

        // Direct mapping: discount (decimal)
        String discountStr = row.get("discount");
        if (discountStr == null || discountStr.trim().isEmpty()) {
            throw new ApiException("Discount cannot be empty");
        }
        
        try {
            BigDecimal discount = new BigDecimal(discountStr.trim());
            if (discount.compareTo(BigDecimal.ZERO) < 0) {
                throw new ApiException("Discount cannot be negative, found: " + discount);
            }
            sales.setDiscount(discount);
        } catch (NumberFormatException e) {
            throw new ApiException("Invalid discount format: '" + discountStr + "'. Expected a decimal number.");
        }

        // Direct mapping: revenue (decimal) - ALLOWS ZERO FOR PROMOTIONAL ITEMS
        String revenueStr = row.get("revenue");
        if (revenueStr == null || revenueStr.trim().isEmpty()) {
            throw new ApiException("Revenue cannot be empty");
        }
        
        try {
            BigDecimal revenue = new BigDecimal(revenueStr.trim());
            if (revenue.compareTo(BigDecimal.ZERO) < 0) {
                throw new ApiException("Revenue cannot be negative, found: " + revenue);
            }
            sales.setRevenue(revenue);
        } catch (NumberFormatException e) {
            throw new ApiException("Invalid revenue format: '" + revenueStr + "'. Expected a non-negative decimal number.");
        }

        return sales;
    }

    /**
     * Normalize string data: trim whitespace and convert to uppercase for consistency.
     * As per PRD requirements for data normalization.
     * 
     * @param value Raw string value from TSV
     * @return Normalized string (empty string if input is null)
     */
    private String normalizeString(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toUpperCase();
    }

    /**
     * Validate that a string is not empty after normalization.
     * 
     * @param value Normalized string value
     * @param fieldName Name of the field for error messages
     * @throws ApiException if value is empty
     */
    private void validateNotEmpty(String value, String fieldName) throws ApiException {
        if (value.isEmpty()) {
            throw new ApiException(fieldName + " cannot be empty");
        }
    }

    /**
     * Get all sales from database (for testing/verification purposes).
     * 
     * @return List of all sales records
     */
    public List<Sales> getAllSales() {
        return salesDao.findAll();
    }

    /**
     * Get sales count for verification purposes.
     * 
     * @return Total number of sales records
     */
    public Long getSalesCount() {
        return salesDao.getTotalSalesCount();
    }

    /**
     * Convert TSV row to Sales entity with comprehensive validation and error tracking.
     * Uses ValidationService for detailed field validation and ErrorTracker for error collection.
     * 
     * @param row TSV row as key-value map
     * @param rowNumber Row number for error reporting
     * @param errorTracker Error tracker for collecting validation errors
     * @return Validated Sales entity, or null if validation fails
     */
    private Sales convertTsvRowToSalesWithValidation(HashMap<String, String> row, int rowNumber, ErrorTracker errorTracker) {
        Sales sales = new Sales();
        boolean hasErrors = false;
        
        // Validate date
        String dayStr = row.get("day");
        ValidationResult dateResult = validationService.validateDate(dayStr);
        if (!dateResult.isValid()) {
            errorTracker.addValidationError(rowNumber, row, "day", dateResult.getErrorMessage());
            hasErrors = true;
        } else {
            try {
                Date salesDate = dateFormatter.parse(dayStr.trim());
                sales.setDate(salesDate);
            } catch (ParseException e) {
                errorTracker.addValidationError(rowNumber, row, "day", "Invalid date format");
                hasErrors = true;
            }
        }
        
        // Validate and lookup SKU
        String skuCode = row.get("sku");
        ValidationResult skuCodeResult = validationService.validateSkuCode(skuCode);
        if (!skuCodeResult.isValid()) {
            errorTracker.addValidationError(rowNumber, row, "sku", skuCodeResult.getErrorMessage());
            hasErrors = true;
        } else {
            try {
                SKU sku = skuService.findBySku(skuCode.trim().toUpperCase());
                sales.setSkuId(sku.getId());
            } catch (ApiException e) {
                // This is a dependency error - SKU not found in master data
                errorTracker.addDependencyError(rowNumber, row, "sku", skuCode.trim().toUpperCase());
                hasErrors = true;
            }
        }
        
        // Validate and lookup store (channel)
        String channel = row.get("channel");
        ValidationResult branchResult = validationService.validateBranch(channel);
        if (!branchResult.isValid()) {
            errorTracker.addValidationError(rowNumber, row, "channel", branchResult.getErrorMessage());
            hasErrors = true;
        } else {
            try {
                Store store = storeService.findByBranch(channel.trim().toUpperCase());
                sales.setStoreId(store.getId());
            } catch (ApiException e) {
                // This is a dependency error - Store not found in master data
                errorTracker.addDependencyError(rowNumber, row, "channel", channel.trim().toUpperCase());
                hasErrors = true;
            }
        }
        
        // Validate quantity
        String quantityStr = row.get("quantity");
        ValidationResult quantityResult = validationService.validateQuantity(quantityStr);
        if (!quantityResult.isValid()) {
            errorTracker.addValidationError(rowNumber, row, "quantity", quantityResult.getErrorMessage());
            hasErrors = true;
        } else {
            try {
                sales.setQuantity(Integer.parseInt(quantityStr.trim()));
            } catch (NumberFormatException e) {
                errorTracker.addValidationError(rowNumber, row, "quantity", "Invalid number format");
                hasErrors = true;
            }
        }
        
        // Validate discount
        String discountStr = row.get("discount");
        ValidationResult discountResult = validationService.validateDiscount(discountStr);
        if (!discountResult.isValid()) {
            errorTracker.addValidationError(rowNumber, row, "discount", discountResult.getErrorMessage());
            hasErrors = true;
        } else {
            try {
                sales.setDiscount(new BigDecimal(discountStr.trim()));
            } catch (NumberFormatException e) {
                errorTracker.addValidationError(rowNumber, row, "discount", "Invalid number format");
                hasErrors = true;
            }
        }
        
        // Validate revenue
        String revenueStr = row.get("revenue");
        ValidationResult revenueResult = validationService.validateRevenue(revenueStr);
        if (!revenueResult.isValid()) {
            errorTracker.addValidationError(rowNumber, row, "revenue", revenueResult.getErrorMessage());
            hasErrors = true;
        } else {
            try {
                sales.setRevenue(new BigDecimal(revenueStr.trim()));
            } catch (NumberFormatException e) {
                errorTracker.addValidationError(rowNumber, row, "revenue", "Invalid number format");
                hasErrors = true;
            }
        }
        
        // Return null if any validation failed
        return hasErrors ? null : sales;
    }

    /**
     * Get sales within a date range (useful for NOOS algorithm).
     * 
     * @param startDate Start date for the range
     * @param endDate End date for the range
     * @return List of sales within the date range
     */
    public List<Sales> getSalesByDateRange(Date startDate, Date endDate) {
        return salesDao.findByDateBetween(startDate, endDate);
    }
}
