package com.iris.increff.service;

import com.iris.increff.controller.UploadResponse;
import com.iris.increff.dao.SalesDao;
import com.iris.increff.model.Sales;
import com.iris.increff.model.SKU;
import com.iris.increff.model.Store;
import com.iris.increff.util.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

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

    @Autowired
    private SalesDao salesDao;

    @Autowired
    private SkuService skuService;

    @Autowired
    private StoreService storeService;

    @Autowired
    private DataClearingService dataClearingService;
    /**
     * Expected date format in sales TSV files
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat(DATE_FORMAT);

    /**
     * Process and save sales from TSV data with graceful error handling.
     * Expected TSV format: day, sku, channel, quantity, discount, revenue
     * Maps to entity fields: date, skuId (via lookup), storeId (via lookup), quantity, discount, revenue
     * 
     * Gracefully handles missing SKUs by logging warnings and skipping those rows.
     * Allows revenue = 0 for promotional items, samples, etc.
     * 
     * @param tsvData Parsed TSV data as list of row maps
     * @return List of warnings (empty if all processed successfully)
     */
    @Transactional
    public UploadResponse processAndSaveSales(ArrayList<HashMap<String, String>> tsvData) {
        UploadResponse response = new UploadResponse();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<String> messages = new ArrayList<>();
        List<Sales> salesToSave = new ArrayList<>();
        int skippedCount = 0;

        // First pass: Validate all data and collect errors/warnings
        for (int i = 0; i < tsvData.size(); i++) {
            HashMap<String, String> row = tsvData.get(i);
            int rowNumber = i + 2; // +2 because: +1 for 0-indexing, +1 for header row
            
            try {
                Sales sales = convertTsvRowToSales(row, warnings, rowNumber);
                if (sales != null) {
                    salesToSave.add(sales);
                } else {
                    skippedCount++;
                }
                
            } catch (Exception e) {
                errors.add("Row " + rowNumber + ": " + e.getMessage());
            }
        }

        // If there are critical errors (not just missing SKUs), fail the upload
        if (!errors.isEmpty()) {
            response.setSuccess(false);
            response.setErrors(errors);
            response.setMessages(messages);
            response.setErrorCount(errors.size());
            return response;
        }

        // Second pass: Database operations (only if no critical errors)
        try {
            // Clear existing data (single-user scenario as per PRD)
            messages.add("Clearing existing data for Sales upload");
            dataClearingService.clearDataForSalesUpload();
            messages.add("Data clearing completed");

            // Save new data in batch
            if (!salesToSave.isEmpty()) {
                messages.add("Saving " + salesToSave.size() + " sales records to database");
                salesDao.saveAll(salesToSave);
                messages.add("Sales upload completed successfully");

                if (skippedCount > 0) {
                    messages.add("Note: " + skippedCount + " rows were skipped due to missing SKUs");
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
        response.setSkippedCount(skippedCount);
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
            System.out.println("WARNING: " + warningMsg);
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
