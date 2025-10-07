package com.iris.increff.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * Task Entity for tracking asynchronous operations
 * 
 * PRD Requirement: "Maintaining Task and Audit Tables 
 * (To know when asynchronous upload, download, algo run, etc. is completed)"
 * 
 * This entity tracks the status of long-running operations like:
 * - File uploads (styles, sales, SKUs, stores)
 * - Algorithm executions (NOOS)
 * - File downloads
 * 
 * @author Anshuk Kawarry
 * @version 1.0
 * @since 2025-01-01
 */
@Entity
@Table(name = "tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_type", length = 50, nullable = false)
    @NotNull(message = "Task type cannot be null")
    private String taskType; // "FILE_UPLOAD", "ALGORITHM_RUN", "FILE_DOWNLOAD"

    @Column(name = "status", length = 20, nullable = false)
    @NotNull(message = "Status cannot be null")
    private String status; // "PENDING", "RUNNING", "COMPLETED", "FAILED"

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "total_records")
    private Integer totalRecords;

    @Column(name = "processed_records")
    private Integer processedRecords;

    @Column(name = "error_count")
    private Integer errorCount;

    @Column(name = "start_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date startTime;

    @Column(name = "end_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date endTime;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "user_id", length = 50)
    private String userId; // For audit trail

    @Column(name = "parameters", length = 2000)
    private String parameters; // Algorithm parameters or upload metadata

    @Column(name = "progress_message", length = 500)
    private String progressMessage; // Human-readable progress message with phase info

    @Column(name = "result_url", length = 500)
    private String resultUrl; // Download URL for completed tasks

    @Column(name = "cancellation_requested")
    private Boolean cancellationRequested = false;

    @Column(name = "created_date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @Column(name = "last_updated_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdatedDate;

    // Pre-persist hook to set timestamps
    @PrePersist
    protected void onCreate() {
        createdDate = new Date();
        lastUpdatedDate = createdDate;
        if (cancellationRequested == null) {
            cancellationRequested = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdatedDate = new Date();
    }

    /**
     * Get progress percentage calculated from processed/total records
     * Returns 0-100 based on actual progress
     */
    public double getProgressPercentage() {
        if (totalRecords == null || totalRecords == 0 || processedRecords == null) {
            return 0.0;
        }
        return (processedRecords * 100.0) / totalRecords;
    }

    /**
     * Update progress with message
     * Progress is tracked via processedRecords/totalRecords
     */
    public void updateProgress(double percentage, String message) {
        this.progressMessage = message;
        // Update processed records to match percentage if totalRecords is set
        if (totalRecords != null && totalRecords > 0) {
            this.processedRecords = (int) ((percentage / 100.0) * totalRecords);
        }
    }

    /**
     * Check if task is still running
     */
    public boolean isRunning() {
        return "PENDING".equals(status) || "RUNNING".equals(status);
    }

    /**
     * Check if task completed successfully
     */
    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }

    /**
     * Check if task failed
     */
    public boolean isFailed() {
        return "FAILED".equals(status);
    }

    /**
     * Check if task was cancelled
     */
    public boolean isCancelled() {
        return "CANCELLED".equals(status);
    }

    /**
     * Check if cancellation was requested
     */
    public boolean isCancellationRequested() {
        return cancellationRequested != null && cancellationRequested;
    }

    /**
     * Request cancellation of this task
     */
    public void requestCancellation() {
        this.cancellationRequested = true;
    }

    /**
     * Validate that task is in a valid state for execution
     * 
     * @return true if task can be executed
     */
    public boolean isValidForExecution() {
        return id != null && 
               taskType != null && 
               status != null &&
               ("PENDING".equals(status) || "RUNNING".equals(status));
    }

    /**
     * Check if task has finished (completed, failed, or cancelled)
     * 
     * @return true if task is in a terminal state
     */
    public boolean isFinished() {
        return isCompleted() || isFailed() || isCancelled();
    }

    /**
     * Get a human-readable status summary
     * 
     * @return Status summary string
     */
    public String getStatusSummary() {
        if (isCompleted()) {
            return String.format("Completed: %d records processed", processedRecords != null ? processedRecords : 0);
        } else if (isFailed()) {
            return String.format("Failed: %s", errorMessage != null ? errorMessage : "Unknown error");
        } else if (isCancelled()) {
            return "Cancelled by user";
        } else if (isRunning()) {
            return String.format("Running: %.1f%% complete", getProgressPercentage());
        } else {
            return "Pending";
        }
    }
}
