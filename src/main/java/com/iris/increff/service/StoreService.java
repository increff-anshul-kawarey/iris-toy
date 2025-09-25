package com.iris.increff.service;

import com.iris.increff.controller.UploadResponse;
import com.iris.increff.dao.StoreDao;
import com.iris.increff.model.Store;
import com.iris.increff.util.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Service for handling Store-related operations including TSV processing.
 * Handles field mapping, validation, normalization, and database operations.
 * 
 * @author Your Name
 * @version 1.0
 * @since 2025-01-01
 */
@Service
public class StoreService {

    @Autowired
    private StoreDao storeDao;

    @Autowired
    private DataClearingService dataClearingService;
    /**
     * Process and save stores from TSV data.
     * Expected TSV format: branch, city
     * Maps directly to entity fields: branch, city
     * 
     * @param tsvData Parsed TSV data as list of row maps
     * @return List of validation errors (empty if all successful)
     */
    @Transactional
    public UploadResponse processAndSaveStores(ArrayList<HashMap<String, String>> tsvData) {
        UploadResponse response = new UploadResponse();
        List<String> errors = new ArrayList<>();
        List<String> messages = new ArrayList<>();
        List<Store> storesToSave = new ArrayList<>();

        // First pass: Validate all data and collect errors
        for (int i = 0; i < tsvData.size(); i++) {
            HashMap<String, String> row = tsvData.get(i);
            int rowNumber = i + 2; // +2 because: +1 for 0-indexing, +1 for header row

            try {
                Store store = convertTsvRowToStore(row);

                // Check for duplicate branches within the uploaded data
                boolean duplicateInBatch = storesToSave.stream()
                    .anyMatch(s -> s.getBranch().equals(store.getBranch()));

                if (duplicateInBatch) {
                    errors.add("Row " + rowNumber + ": Duplicate branch '" +
                               store.getBranch() + "' found within uploaded file");
                    continue;
                }

                storesToSave.add(store);

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
            messages.add("Clearing existing data for Store upload (includes dependent Sales)");
            dataClearingService.clearDataForStoreUpload();
            messages.add("Data clearing completed");

            // Save new data in batch
            if (!storesToSave.isEmpty()) {
                messages.add("Saving " + storesToSave.size() + " stores to database");
                storeDao.saveAll(storesToSave);
                messages.add("Stores upload completed successfully");
            }

        } catch (Exception e) {
            errors.add("Database error: " + e.getMessage());
            response.setSuccess(false);
            response.setErrors(errors);
            response.setMessages(messages);
            response.setErrorCount(errors.size());
            throw new RuntimeException("Failed to save stores to database", e);
        }

        response.setSuccess(true);
        response.setErrors(errors);
        response.setMessages(messages);
        response.setRecordCount(storesToSave.size());
        return response;
    }

    /**
     * Convert a TSV row to Store entity with field mapping and validation.
     * TSV fields map directly to entity fields.
     * 
     * @param row TSV row as key-value map
     * @return Validated Store entity
     * @throws ApiException if validation fails
     */
    private Store convertTsvRowToStore(HashMap<String, String> row) throws ApiException {
        Store store = new Store();
        
        // Direct mapping with normalization
        String branch = normalizeString(row.get("branch"));
        validateNotEmpty(branch, "Branch");
        validateLength(branch, 1, 50, "Branch");
        store.setBranch(branch);

        String city = normalizeString(row.get("city"));
        validateNotEmpty(city, "City");
        validateLength(city, 1, 50, "City");
        store.setCity(city);

        return store;
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
     * Get all stores from database (for testing/verification purposes).
     * 
     * @return List of all stores
     */
    public List<Store> getAllStores() {
        return storeDao.findAll();
    }

    /**
     * Find a store by its branch name (used by other services for lookups).
     * This is critical for Sales TSV processing where "channel" maps to store branch.
     * 
     * @param branch The branch name to search for
     * @return The store if found
     * @throws ApiException if store not found
     */
    public Store findByBranch(String branch) throws ApiException {
        Store store = storeDao.findByBranch(branch);
        if (store == null) {
            throw new ApiException("Store not found with branch: " + branch);
        }
        return store;
    }
}
