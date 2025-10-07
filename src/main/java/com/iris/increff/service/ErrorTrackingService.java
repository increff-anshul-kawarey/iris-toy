package com.iris.increff.service;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Service for tracking and managing validation errors during upload processing
 * 
 * Provides functionality to:
 * - Track failed rows with detailed error reasons
 * - Generate downloadable error reports
 * - Create error files with original data + error reasons
 * 
 * @author Anshuk Kawarry
 * @version 1.0
 * @since 2025-01-01
 */
@Service
public class ErrorTrackingService {

    /**
     * Represents a failed row with its data and error information
     */
    public static class ErrorRow {
        private final int rowNumber;
        private final Map<String, String> originalData;
        private final String errorReason;
        private final String errorType;
        
        public ErrorRow(int rowNumber, Map<String, String> originalData, String errorReason, String errorType) {
            this.rowNumber = rowNumber;
            this.originalData = new HashMap<>(originalData);
            this.errorReason = errorReason;
            this.errorType = errorType;
        }
        
        public int getRowNumber() { return rowNumber; }
        public Map<String, String> getOriginalData() { return originalData; }
        public String getErrorReason() { return errorReason; }
        public String getErrorType() { return errorType; }
    }
    
    /**
     * Container for error tracking during upload processing
     */
    public static class ErrorTracker {
        private final List<ErrorRow> errorRows = new ArrayList<>();
        private final Map<String, Integer> errorTypeCounts = new HashMap<>();
        private final String[] headers;
        
        public ErrorTracker(String[] headers) {
            this.headers = headers.clone();
        }
        
        /**
         * Add an error row to the tracker
         */
        public void addError(int rowNumber, Map<String, String> rowData, String errorReason, String errorType) {
            errorRows.add(new ErrorRow(rowNumber, rowData, errorReason, errorType));
            errorTypeCounts.merge(errorType, 1, Integer::sum);
        }
        
        /**
         * Add a validation error
         */
        public void addValidationError(int rowNumber, Map<String, String> rowData, String field, String errorReason) {
            addError(rowNumber, rowData, field + ": " + errorReason, "VALIDATION_ERROR");
        }
        
        /**
         * Add a dependency error (e.g., missing foreign key) - treated as SKIPPED
         */
        public void addDependencyError(int rowNumber, Map<String, String> rowData, String field, String missingValue) {
            addError(rowNumber, rowData, field + ": '" + missingValue + "' not found in master data", "SKIPPED");
        }
        
        /**
         * Add a skipped row (dependency issues that don't prevent processing)
         */
        public void addSkippedRow(int rowNumber, Map<String, String> rowData, String reason) {
            addError(rowNumber, rowData, reason, "SKIPPED");
        }
        
        /**
         * Add a duplicate error
         */
        public void addDuplicateError(int rowNumber, Map<String, String> rowData, String field, String duplicateValue) {
            addError(rowNumber, rowData, field + ": '" + duplicateValue + "' is duplicate within uploaded file", "DUPLICATE_ERROR");
        }
        
        public List<ErrorRow> getErrorRows() { return new ArrayList<>(errorRows); }
        public Map<String, Integer> getErrorTypeCounts() { return new HashMap<>(errorTypeCounts); }
        public int getTotalErrors() { return errorRows.size(); }
        public boolean hasErrors() { return !errorRows.isEmpty(); }
        public String[] getHeaders() { return headers.clone(); }
        
        /**
         * Get only validation and duplicate errors (not skipped rows)
         */
        public List<ErrorRow> getValidationErrors() {
            return errorRows.stream()
                .filter(row -> !"SKIPPED".equals(row.getErrorType()))
                .collect(java.util.stream.Collectors.toList());
        }
        
        /**
         * Get only skipped rows
         */
        public List<ErrorRow> getSkippedRows() {
            return errorRows.stream()
                .filter(row -> "SKIPPED".equals(row.getErrorType()))
                .collect(java.util.stream.Collectors.toList());
        }
        
        /**
         * Get count of validation errors (excluding skipped)
         */
        public int getValidationErrorCount() {
            return (int) errorRows.stream()
                .filter(row -> !"SKIPPED".equals(row.getErrorType()))
                .count();
        }
        
        /**
         * Get count of skipped rows
         */
        public int getSkippedCount() {
            return errorTypeCounts.getOrDefault("SKIPPED", 0);
        }
        
        /**
         * Check if there are validation errors (excluding skipped rows)
         */
        public boolean hasValidationErrors() {
            return getValidationErrorCount() > 0;
        }
    }
    
    /**
     * Generate error summary for upload response
     */
    public Map<String, Object> generateErrorSummary(ErrorTracker errorTracker) {
        Map<String, Object> summary = new HashMap<>();
        
        summary.put("totalErrors", errorTracker.getTotalErrors());
        summary.put("errorTypeCounts", errorTracker.getErrorTypeCounts());
        
        // Top 5 most common errors
        List<String> topErrors = errorTracker.getErrorRows().stream()
            .map(ErrorRow::getErrorReason)
            .distinct()
            .limit(5)
            .collect(java.util.stream.Collectors.toList());
        summary.put("topErrors", topErrors);
        
        return summary;
    }
    
    /**
     * Generate TSV content for validation error rows (original data only)
     */
    public byte[] generateValidationErrorsTsv(ErrorTracker errorTracker) throws IOException {
        List<ErrorRow> validationErrors = errorTracker.getValidationErrors();
        if (validationErrors.isEmpty()) {
            return new byte[0];
        }
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        // Write headers
        String headerLine = String.join("\t", errorTracker.getHeaders()) + "\n";
        baos.write(headerLine.getBytes(StandardCharsets.UTF_8));
        
        // Write validation error rows
        for (ErrorRow errorRow : validationErrors) {
            List<String> values = new ArrayList<>();
            for (String header : errorTracker.getHeaders()) {
                String value = errorRow.getOriginalData().getOrDefault(header, "");
                values.add(value);
            }
            String dataLine = String.join("\t", values) + "\n";
            baos.write(dataLine.getBytes(StandardCharsets.UTF_8));
        }
        
        return baos.toByteArray();
    }
    
    /**
     * Generate TSV content for skipped rows (original data only)
     */
    public byte[] generateSkippedRowsTsv(ErrorTracker errorTracker) throws IOException {
        List<ErrorRow> skippedRows = errorTracker.getSkippedRows();
        if (skippedRows.isEmpty()) {
            return new byte[0];
        }
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        // Write headers
        String headerLine = String.join("\t", errorTracker.getHeaders()) + "\n";
        baos.write(headerLine.getBytes(StandardCharsets.UTF_8));
        
        // Write skipped rows
        for (ErrorRow errorRow : skippedRows) {
            List<String> values = new ArrayList<>();
            for (String header : errorTracker.getHeaders()) {
                String value = errorRow.getOriginalData().getOrDefault(header, "");
                values.add(value);
            }
            String dataLine = String.join("\t", values) + "\n";
            baos.write(dataLine.getBytes(StandardCharsets.UTF_8));
        }
        
        return baos.toByteArray();
    }
    
    /**
     * Generate TSV content for failed rows with error reasons
     */
    public byte[] generateFailedRowsWithErrorsTsv(ErrorTracker errorTracker) throws IOException {
        if (!errorTracker.hasErrors()) {
            return new byte[0];
        }
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        // Write headers (original + error columns)
        List<String> allHeaders = new ArrayList<>(Arrays.asList(errorTracker.getHeaders()));
        allHeaders.add("Row_Number");
        allHeaders.add("Error_Type");
        allHeaders.add("Error_Reason");
        
        String headerLine = String.join("\t", allHeaders) + "\n";
        baos.write(headerLine.getBytes(StandardCharsets.UTF_8));
        
        // Write failed rows with error information
        for (ErrorRow errorRow : errorTracker.getErrorRows()) {
            List<String> values = new ArrayList<>();
            
            // Original data
            for (String header : errorTracker.getHeaders()) {
                String value = errorRow.getOriginalData().getOrDefault(header, "");
                values.add(value);
            }
            
            // Error information
            values.add(String.valueOf(errorRow.getRowNumber()));
            values.add(errorRow.getErrorType());
            values.add(errorRow.getErrorReason());
            
            String dataLine = String.join("\t", values) + "\n";
            baos.write(dataLine.getBytes(StandardCharsets.UTF_8));
        }
        
        return baos.toByteArray();
    }
    
    /**
     * Generate error report summary as TSV
     */
    public byte[] generateErrorReportSummary(ErrorTracker errorTracker) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        // Summary header
        baos.write("Error Summary Report\n".getBytes(StandardCharsets.UTF_8));
        baos.write(("Total Errors: " + errorTracker.getTotalErrors() + "\n\n").getBytes(StandardCharsets.UTF_8));
        
        // Error type breakdown
        baos.write("Error Type Breakdown:\n".getBytes(StandardCharsets.UTF_8));
        baos.write("Error_Type\tCount\n".getBytes(StandardCharsets.UTF_8));
        
        for (Map.Entry<String, Integer> entry : errorTracker.getErrorTypeCounts().entrySet()) {
            String line = entry.getKey() + "\t" + entry.getValue() + "\n";
            baos.write(line.getBytes(StandardCharsets.UTF_8));
        }
        
        return baos.toByteArray();
    }
    
    /**
     * Save error files to temporary location and return file paths
     */
    public Map<String, String> saveErrorFiles(ErrorTracker errorTracker, String fileType, String taskId) throws IOException {
        Map<String, String> filePaths = new HashMap<>();
        
        if (!errorTracker.hasErrors()) {
            return filePaths;
        }
        
        String baseDir = System.getProperty("java.io.tmpdir");
        String timestamp = String.valueOf(System.currentTimeMillis());
        
        try {
            // Validation errors file (original data only)
            if (errorTracker.hasValidationErrors()) {
                String validationErrorsFileName = String.format("validation_errors_%s_%s_%s.tsv", fileType.toLowerCase(), taskId, timestamp);
                String validationErrorsPath = baseDir + "/" + validationErrorsFileName;
                java.nio.file.Files.write(java.nio.file.Paths.get(validationErrorsPath), generateValidationErrorsTsv(errorTracker));
                filePaths.put("validationErrors", validationErrorsPath);
            }
            
            // Skipped rows file (original data only)
            if (errorTracker.getSkippedCount() > 0) {
                String skippedRowsFileName = String.format("skipped_rows_%s_%s_%s.tsv", fileType.toLowerCase(), taskId, timestamp);
                String skippedRowsPath = baseDir + "/" + skippedRowsFileName;
                java.nio.file.Files.write(java.nio.file.Paths.get(skippedRowsPath), generateSkippedRowsTsv(errorTracker));
                filePaths.put("skippedRows", skippedRowsPath);
            }
            
            // All failed rows with error reasons (comprehensive file)
            String failedRowsWithErrorsFileName = String.format("all_failed_rows_with_errors_%s_%s_%s.tsv", fileType.toLowerCase(), taskId, timestamp);
            String failedRowsWithErrorsPath = baseDir + "/" + failedRowsWithErrorsFileName;
            java.nio.file.Files.write(java.nio.file.Paths.get(failedRowsWithErrorsPath), generateFailedRowsWithErrorsTsv(errorTracker));
            filePaths.put("allFailedRowsWithErrors", failedRowsWithErrorsPath);
            
            // Error summary file
            String errorSummaryFileName = String.format("error_summary_%s_%s_%s.tsv", fileType.toLowerCase(), taskId, timestamp);
            String errorSummaryPath = baseDir + "/" + errorSummaryFileName;
            java.nio.file.Files.write(java.nio.file.Paths.get(errorSummaryPath), generateErrorReportSummary(errorTracker));
            filePaths.put("errorSummary", errorSummaryPath);
            
        } catch (IOException e) {
            // Clean up any partially created files
            filePaths.values().forEach(path -> {
                try {
                    java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(path));
                } catch (IOException ignored) {}
            });
            throw e;
        }
        
        return filePaths;
    }
}
