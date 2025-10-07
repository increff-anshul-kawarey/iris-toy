package com.iris.increff.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Enhanced response class for upload operations with detailed error tracking
 */
public class UploadResponse {
    private boolean success;
    private String message;
    private List<String> messages;
    private List<String> errors;
    private List<String> warnings;
    private Integer recordCount;
    private Integer errorCount;
    private Integer skippedCount;
    
    // Enhanced error tracking fields
    private Map<String, Object> errorSummary;
    private Map<String, String> errorFiles;
    private Integer validationErrors;
    private Integer dependencyErrors;
    private Integer duplicateErrors;

    // Constructors, getters, and setters
    public UploadResponse() {}

    public UploadResponse(boolean success, String message, List<String> messages,
                         List<String> errors, Integer recordCount) {
        this.success = success;
        this.message = message;
        this.messages = messages != null ? messages : new ArrayList<>();
        this.errors = errors != null ? errors : new ArrayList<>();
        this.recordCount = recordCount;
        this.errorCount = errors != null ? errors.size() : 0;
    }

    // Getters and setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public List<String> getMessages() { return messages; }
    public void setMessages(List<String> messages) { this.messages = messages; }

    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors; }

    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings; }

    public Integer getRecordCount() { return recordCount; }
    public void setRecordCount(Integer recordCount) { this.recordCount = recordCount; }

    public Integer getErrorCount() { return errorCount; }
    public void setErrorCount(Integer errorCount) { this.errorCount = errorCount; }

    public Integer getSkippedCount() { return skippedCount; }
    public void setSkippedCount(Integer skippedCount) { this.skippedCount = skippedCount; }

    // Enhanced error tracking getters and setters
    public Map<String, Object> getErrorSummary() { return errorSummary; }
    public void setErrorSummary(Map<String, Object> errorSummary) { this.errorSummary = errorSummary; }

    public Map<String, String> getErrorFiles() { return errorFiles; }
    public void setErrorFiles(Map<String, String> errorFiles) { this.errorFiles = errorFiles; }

    public Integer getValidationErrors() { return validationErrors; }
    public void setValidationErrors(Integer validationErrors) { this.validationErrors = validationErrors; }

    public Integer getDependencyErrors() { return dependencyErrors; }
    public void setDependencyErrors(Integer dependencyErrors) { this.dependencyErrors = dependencyErrors; }

    public Integer getDuplicateErrors() { return duplicateErrors; }
    public void setDuplicateErrors(Integer duplicateErrors) { this.duplicateErrors = duplicateErrors; }
    
    /**
     * Set error tracking information from ErrorTracker
     */
    public void setErrorTrackingInfo(Map<String, Object> errorSummary, Map<String, String> errorFiles) {
        this.errorSummary = errorSummary;
        this.errorFiles = errorFiles;
        
        if (errorSummary != null) {
            @SuppressWarnings("unchecked")
            Map<String, Integer> errorTypeCounts = (Map<String, Integer>) errorSummary.get("errorTypeCounts");
            if (errorTypeCounts != null) {
                this.validationErrors = errorTypeCounts.getOrDefault("VALIDATION_ERROR", 0);
                this.dependencyErrors = errorTypeCounts.getOrDefault("DEPENDENCY_ERROR", 0);
                this.duplicateErrors = errorTypeCounts.getOrDefault("DUPLICATE_ERROR", 0);
            }
        }
    }
}
