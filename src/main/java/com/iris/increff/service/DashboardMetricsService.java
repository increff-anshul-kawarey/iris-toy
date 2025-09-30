package com.iris.increff.service;

import com.iris.increff.dao.*;
import com.iris.increff.model.DashBoardData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Dashboard Metrics Service
 * 
 * Provides real-time system metrics for the dashboard:
 * - Data volume statistics (sales, SKUs, stores, styles)
 * - Upload activity and success rates
 * - Processing status and task monitoring
 * 
 * Replaces dummy data with actual system metrics.
 * 
 * @author Anshuk Kawarry
 * @version 1.0
 * @since 2025-01-01
 */
@Service
public class DashboardMetricsService {

    private static final Logger logger = LoggerFactory.getLogger(DashboardMetricsService.class);

    @Autowired
    private SalesDao salesDao;

    @Autowired
    private SkuDao skuDao;

    @Autowired
    private StoreDao storeDao;

    @Autowired
    private StyleDao styleDao;

    @Autowired
    private TaskDao taskDao;

    /**
     * Get comprehensive dashboard metrics
     * 
     * @return DashBoardData with real system metrics
     */
    @Transactional(readOnly = true)
    public DashBoardData getDashboardMetrics() {
        logger.info("📊 Collecting dashboard metrics...");
        
        try {
            // Tile 1: Sales Data Records
            long totalSalesRecords = salesDao.getTotalSalesCount();
            String salesDataStatus = getSalesDataStatus(totalSalesRecords);
            
            // Tile 2: Master Data Counts
            long totalSkus = skuDao.getTotalSkuCount();
            long totalStores = storeDao.getTotalStoreCount();
            long totalStyles = styleDao.getTotalStyleCount();
            String masterDataStatus = getMasterDataStatus(totalSkus, totalStores, totalStyles);
            
            // Tile 3: Recent Activity (last 7 days)
            long[] recentStats = taskDao.getRecentTaskStats(7);
            int recentUploads = (int) recentStats[0];
            double uploadSuccessRate = recentStats[0] > 0 ? (recentStats[1] * 100.0) / recentStats[0] : 0.0;
            String recentActivityStatus = getRecentActivityStatus(recentUploads, uploadSuccessRate);
            
            // Tile 4: Processing Status
            int activeTasks = taskDao.getActiveTaskCount();
            int pendingTasks = (int) taskDao.countByStatus("PENDING");
            String processingStatus = getProcessingStatus(activeTasks, pendingTasks);
            
            logger.info("✅ Dashboard metrics collected successfully: {} sales, {} SKUs, {} stores, {} styles", 
                       totalSalesRecords, totalSkus, totalStores, totalStyles);
            
            return new DashBoardData(
                totalSalesRecords, salesDataStatus,
                totalSkus, totalStores, totalStyles, masterDataStatus,
                recentUploads, uploadSuccessRate, recentActivityStatus,
                activeTasks, pendingTasks, processingStatus
            );
            
        } catch (Exception e) {
            logger.error("❌ Failed to collect dashboard metrics: {}", e.getMessage(), e);
            
            // Return safe fallback data
            return new DashBoardData(
                0L, "Data unavailable",
                0L, 0L, 0L, "Data unavailable",
                0, 0.0, "Data unavailable",
                0, 0, "System unavailable"
            );
        }
    }

    /**
     * Determine sales data status message
     */
    private String getSalesDataStatus(long totalSalesRecords) {
        if (totalSalesRecords == 0) {
            return "No data available";
        } else if (totalSalesRecords < 1000) {
            return "Limited data";
        } else if (totalSalesRecords < 10000) {
            return "Good data volume";
        } else {
            return "Rich data available";
        }
    }

    /**
     * Determine master data status message
     */
    private String getMasterDataStatus(long totalSkus, long totalStores, long totalStyles) {
        long totalMasterRecords = totalSkus + totalStores + totalStyles;
        
        if (totalMasterRecords == 0) {
            return "Setup required";
        } else if (totalSkus > 0 && totalStores > 0 && totalStyles > 0) {
            return "Complete setup";
        } else {
            return "Partial setup";
        }
    }

    /**
     * Determine recent activity status message
     */
    private String getRecentActivityStatus(int recentUploads, double uploadSuccessRate) {
        if (recentUploads == 0) {
            return "No recent activity";
        } else if (uploadSuccessRate >= 80.0) {
            return "Healthy activity";
        } else if (uploadSuccessRate >= 50.0) {
            return "Some issues";
        } else {
            return "Needs attention";
        }
    }

    /**
     * Determine processing status message
     */
    private String getProcessingStatus(int activeTasks, int pendingTasks) {
        int totalActiveTasks = activeTasks + pendingTasks;
        
        if (totalActiveTasks == 0) {
            return "System idle";
        } else if (totalActiveTasks < 5) {
            return "Normal load";
        } else if (totalActiveTasks < 10) {
            return "High activity";
        } else {
            return "Heavy load";
        }
    }
}
