package com.iris.increff.controller;

import java.util.ArrayList;
import java.util.List;

/**
 * Response class for upload operations to avoid unsafe casting
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
}
