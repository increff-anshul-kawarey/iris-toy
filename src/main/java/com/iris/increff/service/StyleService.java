package com.iris.increff.service;

import com.iris.increff.controller.UploadResponse;
import com.iris.increff.dao.StyleDao;
import com.iris.increff.model.Style;
import com.iris.increff.util.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Service for handling Style-related operations including TSV processing.
 * Handles field mapping, validation, normalization, and database operations.
 * 
 * @author Your Name
 * @version 1.0
 * @since 2025-01-01
 */
@Service
public class StyleService {

    @Autowired
    private StyleDao styleDao;

    @Autowired
    private DataClearingService dataClearingService;


    /**
     * Process and save styles from TSV data.
     * Expected TSV format: style, brand, category, sub_category, mrp, gender
     * Maps to entity fields: styleCode, brand, category, subCategory, mrp, gender
     *
     * @param tsvData Parsed TSV data as list of row maps
     * @return UploadResponse with success status and messages
     */
    @Transactional
    public UploadResponse processAndSaveStyles(ArrayList<HashMap<String, String>> tsvData) {
        UploadResponse response = new UploadResponse();
        List<String> errors = new ArrayList<>();
        List<String> messages = new ArrayList<>();
        List<Style> stylesToSave = new ArrayList<>();

        // First pass: Validate all data and collect errors
        for (int i = 0; i < tsvData.size(); i++) {
            HashMap<String, String> row = tsvData.get(i);
            int rowNumber = i + 2; // +2 because: +1 for 0-indexing, +1 for header row

            try {
                Style style = convertTsvRowToStyle(row);

                // Check for duplicate style codes within the uploaded data
                boolean duplicateInBatch = stylesToSave.stream()
                    .anyMatch(s -> s.getStyleCode().equals(style.getStyleCode()));

                if (duplicateInBatch) {
                    errors.add("Row " + rowNumber + ": Duplicate style code '" +
                               style.getStyleCode() + "' found within uploaded file");
                    continue;
                }

                stylesToSave.add(style);

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
            messages.add("Clearing existing data for Style upload (includes dependent SKUs and Sales)");
            dataClearingService.clearDataForStyleUpload();
            messages.add("Data clearing completed");

            // Save new data in batch
            if (!stylesToSave.isEmpty()) {
                messages.add("Saving " + stylesToSave.size() + " styles to database");
                styleDao.saveAll(stylesToSave);
                messages.add("Styles upload completed successfully");
            }

        } catch (Exception e) {
            errors.add("Database error: " + e.getMessage());
            response.setSuccess(false);
            response.setErrors(errors);
            response.setMessages(messages);
            response.setErrorCount(errors.size());
            throw new RuntimeException("Failed to save styles to database", e);
        }

        response.setSuccess(true);
        response.setErrors(errors);
        response.setMessages(messages);
        response.setRecordCount(stylesToSave.size());
        return response;
    }

    /**
     * Convert a TSV row to Style entity with field mapping and validation.
     * TSV fields → Entity fields mapping:
     * - "style" → styleCode
     * - "sub_category" → subCategory (camelCase conversion)
     * - Other fields map directly
     * 
     * @param row TSV row as key-value map
     * @return Validated Style entity
     * @throws ApiException if validation fails
     */
    private Style convertTsvRowToStyle(HashMap<String, String> row) throws ApiException {
        Style style = new Style();
        
        // Field mapping: "style" → styleCode
        String styleCode = normalizeString(row.get("style"));
        validateNotEmpty(styleCode, "Style code");
        validateLength(styleCode, 1, 50, "Style code");
        style.setStyleCode(styleCode);

        // Direct mapping with normalization
        String brand = normalizeString(row.get("brand"));
        validateNotEmpty(brand, "Brand");
        validateLength(brand, 1, 50, "Brand");
        style.setBrand(brand);

        String category = normalizeString(row.get("category"));
        validateNotEmpty(category, "Category");
        validateLength(category, 1, 50, "Category");
        style.setCategory(category);

        // Field mapping: "sub_category" → subCategory (camelCase)
        String subCategory = normalizeString(row.get("sub_category"));
        validateNotEmpty(subCategory, "Sub-category");
        validateLength(subCategory, 1, 50, "Sub-category");
        style.setSubCategory(subCategory);

        // MRP validation and parsing
        String mrpStr = row.get("mrp");
        if (mrpStr == null || mrpStr.trim().isEmpty()) {
            throw new ApiException("MRP cannot be empty");
        }
        
        try {
            BigDecimal mrp = new BigDecimal(mrpStr.trim());
            if (mrp.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ApiException("MRP must be greater than 0, found: " + mrp);
            }
            style.setMrp(mrp);
        } catch (NumberFormatException e) {
            throw new ApiException("Invalid MRP format: '" + mrpStr + "'. Expected a decimal number.");
        }

        String gender = normalizeString(row.get("gender"));
        validateNotEmpty(gender, "Gender");
        validateLength(gender, 1, 50, "Gender");
        style.setGender(gender);

        return style;
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
     * Get all styles from database (for testing/verification purposes).
     * 
     * @return List of all styles
     */
    public List<Style> getAllStyles() {
        return styleDao.findAll();
    }

    /**
     * Find a style by its style code (used by other services for lookups).
     * 
     * @param styleCode The style code to search for
     * @return The style if found
     * @throws ApiException if style not found
     */
    public Style findByStyleCode(String styleCode) throws ApiException {
        Style style = styleDao.findByStyleCode(styleCode);
        if (style == null) {
            throw new ApiException("Style not found with code: " + styleCode);
        }
        return style;
    }
}
