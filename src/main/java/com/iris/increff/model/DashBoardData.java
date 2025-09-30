package com.iris.increff.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Dashboard Data Model for System Metrics
 * 
 * Contains real-time system statistics for dashboard display:
 * - Tile 1: Total Sales Records
 * - Tile 2: Master Data Counts (SKUs, Stores, Styles)
 * - Tile 3: Recent Upload Activity
 * - Tile 4: Processing Status
 * 
 * @author Anshuk Kawarry
 * @version 2.0
 * @since 2025-01-01
 */
@Getter
@Setter
public class DashBoardData {

    // Tile 1: Data Records
    private long totalSalesRecords;
    private String salesDataStatus;
    
    // Tile 2: Master Data
    private long totalSkus;
    private long totalStores;
    private long totalStyles;
    private String masterDataStatus;
    
    // Tile 3: Recent Activity
    private int recentUploads;
    private double uploadSuccessRate;
    private String recentActivityStatus;
    
    // Tile 4: Processing Status
    private int activeTasks;
    private int pendingTasks;
    private String processingStatus;

    /**
     * Constructor for modern dashboard with real metrics
     */
    public DashBoardData(long totalSalesRecords, String salesDataStatus,
                        long totalSkus, long totalStores, long totalStyles, String masterDataStatus,
                        int recentUploads, double uploadSuccessRate, String recentActivityStatus,
                        int activeTasks, int pendingTasks, String processingStatus) {
        this.totalSalesRecords = totalSalesRecords;
        this.salesDataStatus = salesDataStatus;
        this.totalSkus = totalSkus;
        this.totalStores = totalStores;
        this.totalStyles = totalStyles;
        this.masterDataStatus = masterDataStatus;
        this.recentUploads = recentUploads;
        this.uploadSuccessRate = uploadSuccessRate;
        this.recentActivityStatus = recentActivityStatus;
        this.activeTasks = activeTasks;
        this.pendingTasks = pendingTasks;
        this.processingStatus = processingStatus;
        
    }

    /**
     * Default constructor
     */
    public DashBoardData() {
    }
}
