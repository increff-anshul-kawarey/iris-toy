package com.iris.increff.service;

import com.iris.increff.controller.UploadResponse;
import com.iris.increff.dao.SkuDao;
import com.iris.increff.model.SKU;
import com.iris.increff.model.Style;
import com.iris.increff.util.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Service for handling SKU-related operations including TSV processing.
 * Handles field mapping, validation, normalization, and database operations.
 * Depends on StyleService for style lookups.
 * 
 * @author Your Name
 * @version 1.0
 * @since 2025-01-01
 */
@Service
public class SkuService {

    @Autowired
    private SkuDao skuDao;

    @Autowired
    private DataClearingService dataClearingService;
    @Autowired
    private StyleService styleService;

    /**
     * Process and save SKUs from TSV data.
     * Expected TSV format: sku, style, size
     * Maps to entity fields: sku, styleId (via lookup), size
     * 
     * @param tsvData Parsed TSV data as list of row maps
     * @return List of validation errors (empty if all successful)
     */
    @Transactional
    public UploadResponse processAndSaveSKUs(ArrayList<HashMap<String, String>> tsvData) {
        UploadResponse response = new UploadResponse();
        List<String> errors = new ArrayList<>();
        List<String> messages = new ArrayList<>();
        List<SKU> skusToSave = new ArrayList<>();

        // First pass: Validate all data and collect errors
        for (int i = 0; i < tsvData.size(); i++) {
            HashMap<String, String> row = tsvData.get(i);
            int rowNumber = i + 2; // +2 because: +1 for 0-indexing, +1 for header row

            try {
                SKU sku = convertTsvRowToSKU(row);

                // Check for duplicate SKU codes within the uploaded data
                boolean duplicateInBatch = skusToSave.stream()
                    .anyMatch(s -> s.getSku().equals(sku.getSku()));

                if (duplicateInBatch) {
                    errors.add("Row " + rowNumber + ": Duplicate SKU code '" +
                               sku.getSku() + "' found within uploaded file");
                    continue;
                }

                skusToSave.add(sku);

            } catch (Exception e) {
                errors.add("Row " + rowNumber + ": " + e.getMessage());
            }
        }

        // If there are validation errors, don't proceed with database operations
        if (!errors.isEmpty()) {
            response.setSuccess(false);
            response.setErrors(errors);
            response.setMessages(messages);
            response.setErrorCount(errors.size());
            return response;
        }

        // Second pass: Database operations (only if all data is valid)
        try {
            // Clear existing data (single-user scenario as per PRD)
            messages.add("Clearing existing data for SKU upload (includes dependent Sales)");
            dataClearingService.clearDataForSkuUpload();
            messages.add("Data clearing completed");

            // Save new data in batch
            if (!skusToSave.isEmpty()) {
                messages.add("Saving " + skusToSave.size() + " SKUs to database");
                skuDao.saveAll(skusToSave);
                messages.add("SKUs upload completed successfully");
            }

        } catch (Exception e) {
            errors.add("Database error: " + e.getMessage());
            response.setSuccess(false);
            response.setErrors(errors);
            response.setMessages(messages);
            response.setErrorCount(errors.size());
            throw new RuntimeException("Failed to save SKUs to database", e);
        }

        response.setSuccess(true);
        response.setErrors(errors);
        response.setMessages(messages);
        response.setRecordCount(skusToSave.size());
        return response;
    }

    /**
     * Convert a TSV row to SKU entity with field mapping and validation.
     * TSV fields → Entity fields mapping:
     * - "sku" → sku (direct)
     * - "style" → styleId (via lookup in Style table)
     * - "size" → size (direct)
     * 
     * @param row TSV row as key-value map
     * @return Validated SKU entity
     * @throws ApiException if validation fails
     */
    private SKU convertTsvRowToSKU(HashMap<String, String> row) throws ApiException {
        SKU sku = new SKU();
        
        // Direct mapping: sku code
        String skuCode = normalizeString(row.get("sku"));
        validateNotEmpty(skuCode, "SKU code");
        validateLength(skuCode, 1, 50, "SKU code");
        sku.setSku(skuCode);

        // Lookup mapping: "style" → styleId
        String styleCode = normalizeString(row.get("style"));
        validateNotEmpty(styleCode, "Style code");
        
        try {
            Style style = styleService.findByStyleCode(styleCode);
            sku.setStyleId(style.getId());
        } catch (ApiException e) {
            // Re-throw with more context for the row being processed
            throw new ApiException("Style lookup failed for '" + styleCode + "': " + e.getMessage());
        }

        // Direct mapping: size
        String size = normalizeString(row.get("size"));
        validateNotEmpty(size, "Size");
        validateLength(size, 1, 10, "Size");
        sku.setSize(size);

        return sku;
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
     * Validate string length constraints.
     * 
     * @param value String to validate
     * @param minLength Minimum allowed length
     * @param maxLength Maximum allowed length
     * @param fieldName Name of the field for error messages
     * @throws ApiException if length constraints are violated
     */
    private void validateLength(String value, int minLength, int maxLength, String fieldName) 
            throws ApiException {
        if (value.length() < minLength || value.length() > maxLength) {
            throw new ApiException(fieldName + " must be between " + minLength + 
                                 " and " + maxLength + " characters, found: " + value.length());
        }
    }

    /**
     * Get all SKUs from database (for testing/verification purposes).
     * 
     * @return List of all SKUs
     */
    public List<SKU> getAllSKUs() {
        return skuDao.findAll();
    }

    /**
     * Find a SKU by its SKU code (used by other services for lookups).
     * This is critical for Sales TSV processing where "sku" maps to SKU ID.
     * 
     * @param skuCode The SKU code to search for
     * @return The SKU if found
     * @throws ApiException if SKU not found
     */
    public SKU findBySku(String skuCode) throws ApiException {
        SKU sku = skuDao.findBySku(skuCode);
        if (sku == null) {
            throw new ApiException("SKU not found with code: " + skuCode);
        }
        return sku;
    }
}
