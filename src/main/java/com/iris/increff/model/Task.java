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
    private String parameters; // JSON string of algorithm parameters

    // Enhanced fields for async progress tracking
    @Column(name = "progress_percentage")
    private Double progressPercentage = 0.0; // 0.0 to 100.0

    @Column(name = "current_phase", length = 50)
    private String currentPhase; // "DATA_LOADING", "PROCESSING", "CLASSIFICATION", etc.

    @Column(name = "current_step")
    private Integer currentStep; // Current step number

    @Column(name = "total_steps")
    private Integer totalSteps; // Total expected steps

    @Column(name = "result_url", length = 500)
    private String resultUrl; // Download URL for completed tasks

    @Column(name = "result_type", length = 50)
    private String resultType; // "TSV", "JSON", etc.

    @Column(name = "metadata", length = 4000)
    private String metadata; // JSON string for additional progress info

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
        if (progressPercentage == null) {
            progressPercentage = 0.0;
        }
        if (cancellationRequested == null) {
            cancellationRequested = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdatedDate = new Date();
    }

    /**
     * Get progress percentage (backward compatibility)
     */
    public double getProgressPercentage() {
        if (progressPercentage != null) {
            return progressPercentage;
        }
        // Fallback to calculation if not set
        if (totalRecords == null || totalRecords == 0 || processedRecords == null) {
            return 0.0;
        }
        return (processedRecords * 100.0) / totalRecords;
    }

    /**
     * Update progress with phase information
     */
    public void updateProgress(double percentage, String phase, String message) {
        this.progressPercentage = percentage;
        this.currentPhase = phase;
        
        // Store message in metadata if provided
        if (message != null && !message.isEmpty()) {
            this.metadata = String.format("{\"message\":\"%s\",\"timestamp\":\"%s\"}", 
                                        message, new Date().toString());
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
}
