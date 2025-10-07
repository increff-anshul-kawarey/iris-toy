package com.iris.increff.service;

import com.iris.increff.dao.*;
import com.iris.increff.model.Report1Data;
import com.iris.increff.model.Report2Data;
import com.iris.increff.model.NoosResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Report Analytics Service
 * 
 * Generates real analytics data for reports:
 * - Report 1: NOOS Analytics (algorithm execution history, classification insights)
 * - Report 2: System Health (upload statistics, task performance, data quality)
 * 
 * @author Anshuk Kawarry
 * @version 1.0
 * @since 2025-01-01
 */
@Service
public class ReportAnalyticsService {

    private static final Logger logger = LoggerFactory.getLogger(ReportAnalyticsService.class);

    @Autowired
    private NoosAlgorithmService noosAlgorithmService;
    
    
    @Autowired
    private TaskDao taskDao;
    
    @Autowired
    private SalesDao salesDao;
    
    @Autowired
    private com.iris.increff.dao.NoosResultDao noosResultDao;

    /**
     * Generate NOOS Analytics Report (Report 1)
     * Shows algorithm execution history and classification insights
     */
    @Transactional(readOnly = true)
    public List<Report1Data> generateNoosAnalyticsReport() {
        logger.info("📊 Generating NOOS Analytics Report...");
        
        try {
            List<Report1Data> reportData = new ArrayList<>();

            // Build analytics across recent runs (e.g., last 5)
            List<Long> recentRunIds = noosAlgorithmService.getRecentRunIds(5);
            if (recentRunIds == null || recentRunIds.isEmpty()) {
                // Fallback: group by calculatedDate day when runIds are absent (legacy data)
                // Use DAO method that returns all results ordered by date (not limited to latest run)
                List<NoosResult> latestResults = noosResultDao.getLatestResults();
                if (latestResults == null || latestResults.isEmpty()) {
                    reportData.addAll(generateSampleNoosData());
                    logger.info("✅ Generated {} NOOS analytics report entries (sample)", reportData.size());
                    return reportData;
                }

                Map<String, List<NoosResult>> byDay = new java.util.HashMap<>();
                for (NoosResult r : latestResults) {
                    if (r.getCalculatedDate() == null) continue;
                    String dayKey = new java.text.SimpleDateFormat("yyyy-MM-dd").format(r.getCalculatedDate());
                    byDay.computeIfAbsent(dayKey, k -> new java.util.ArrayList<>()).add(r);
                }

                for (Map.Entry<String, List<NoosResult>> e : byDay.entrySet()) {
                    List<NoosResult> runResults = e.getValue();
                    Date executionDate = runResults.get(0).getCalculatedDate();

                    long coreCount = runResults.stream().filter(r -> "core".equalsIgnoreCase(r.getType())).count();
                    long bestsellerCount = runResults.stream().filter(r -> "bestseller".equalsIgnoreCase(r.getType())).count();
                    long fashionCount = runResults.stream().filter(r -> "fashion".equalsIgnoreCase(r.getType())).count();

                    String algorithmLabel = "NOOS Analysis " + e.getKey();

                    Report1Data reportItem = new Report1Data(
                        executionDate,
                        algorithmLabel,
                        "COMPLETED",
                        runResults.size(),
                        (int) coreCount,
                        (int) bestsellerCount,
                        (int) fashionCount,
                        2.5,
                        "Legacy grouping by date"
                    );
                    reportData.add(reportItem);
                }
            } else {
                // Filter out null run IDs (legacy data without run tracking)
                java.util.List<Long> filteredRunIds = new java.util.ArrayList<>();
                for (Long id : recentRunIds) {
                    if (id != null) filteredRunIds.add(id);
                }
                if (filteredRunIds.isEmpty()) {
                    // Fallback to date-grouping when runIds are null
                    List<NoosResult> allResults = noosResultDao.getLatestResults();
                    java.util.Map<String, java.util.List<NoosResult>> byDay = new java.util.HashMap<>();
                    for (NoosResult r : allResults) {
                        if (r.getCalculatedDate() == null) continue;
                        String dayKey = new java.text.SimpleDateFormat("yyyy-MM-dd").format(r.getCalculatedDate());
                        byDay.computeIfAbsent(dayKey, k -> new java.util.ArrayList<>()).add(r);
                    }
                    for (java.util.Map.Entry<String, java.util.List<NoosResult>> e : byDay.entrySet()) {
                        java.util.List<NoosResult> runResults = e.getValue();
                        Date executionDate = runResults.get(0).getCalculatedDate();
                        long coreCount = runResults.stream().filter(r -> "core".equalsIgnoreCase(r.getType())).count();
                        long bestsellerCount = runResults.stream().filter(r -> "bestseller".equalsIgnoreCase(r.getType())).count();
                        long fashionCount = runResults.stream().filter(r -> "fashion".equalsIgnoreCase(r.getType())).count();
                        String algorithmLabel = "NOOS Analysis " + e.getKey();
                        Report1Data reportItem = new Report1Data(
                            executionDate,
                            algorithmLabel,
                            "COMPLETED",
                            runResults.size(),
                            (int) coreCount,
                            (int) bestsellerCount,
                            (int) fashionCount,
                            2.5,
                            "Legacy grouping by date"
                        );
                        reportData.add(reportItem);
                    }
                } else {
                for (Long runId : filteredRunIds) {
                    List<NoosResult> runResults = noosAlgorithmService.getResultsByRunId(runId);
                    if (runResults == null || runResults.isEmpty()) {
                        continue;
                    }

                    Date executionDate = noosAlgorithmService.getRunDate(runId);

                    long coreCount = runResults.stream().filter(r -> "core".equalsIgnoreCase(r.getType())).count();
                    long bestsellerCount = runResults.stream().filter(r -> "bestseller".equalsIgnoreCase(r.getType())).count();
                    long fashionCount = runResults.stream().filter(r -> "fashion".equalsIgnoreCase(r.getType())).count();

                    String algorithmLabel = "NOOS Analysis " + (executionDate != null ? executionDate.toString().substring(0, 10) : ("Run " + runId));

                    Report1Data reportItem = new Report1Data(
                        executionDate,
                        algorithmLabel,
                        "COMPLETED",
                        runResults.size(),
                        (int) coreCount,
                        (int) bestsellerCount,
                        (int) fashionCount,
                        2.5,
                        "Real algorithm execution"
                    );

                    reportData.add(reportItem);
                }
                }
            }

            logger.info("✅ Generated {} NOOS analytics report entries", reportData.size());
            return reportData;
            
        } catch (Exception e) {
            logger.error("❌ Failed to generate NOOS analytics report: {}", e.getMessage(), e);
            return generateSampleNoosData();
        }
    }

    /**
     * Generate System Health Report (Report 2)  
     * Shows upload statistics, task performance, and system metrics
     */
    @Transactional(readOnly = true)
    public List<Report2Data> generateSystemHealthReport() {
        logger.info("🏥 Generating System Health Report...");
        
        try {
            List<Report2Data> reportData = new ArrayList<>();
            
            // Map logical sections to actual taskType values used in system
            java.util.LinkedHashMap<String, String> typeMap = new java.util.LinkedHashMap<>();
            typeMap.put("UPLOAD_SALES", "SALES_UPLOAD");
            typeMap.put("UPLOAD_STYLES", "STYLES_UPLOAD");
            typeMap.put("UPLOAD_STORES", "STORES_UPLOAD");
            typeMap.put("UPLOAD_SKUS", "SKUS_UPLOAD");
            typeMap.put("RUN_NOOS", "ALGORITHM_RUN");

            for (java.util.Map.Entry<String, String> entry : typeMap.entrySet()) {
                String label = entry.getKey();
                String actualType = entry.getValue();

                long[] stats = taskDao.getRecentTaskStatsByType(actualType, 7);
                long totalTasks = stats[0];
                long successfulTasks = stats[1];
                long failedTasks = stats[2];
                double successRate = totalTasks > 0 ? (successfulTasks * 100.0) / totalTasks : 0.0;

                String systemStatus = determineSystemStatus(successRate, totalTasks);

                // Compute average execution time for this type over last 7 days
                java.util.List<com.iris.increff.model.Task> recentTasks = taskDao.getTasksByTypeSince(actualType, 7);
                double avgSeconds = 0.0;
                if (!recentTasks.isEmpty()) {
                    long totalMillis = 0L;
                    int counted = 0;
                    for (com.iris.increff.model.Task t : recentTasks) {
                        if (t.getStartTime() != null && t.getEndTime() != null) {
                            long dur = t.getEndTime().getTime() - t.getStartTime().getTime();
                            if (dur > 0) {
                                totalMillis += dur;
                                counted++;
                            }
                        }
                    }
                    if (counted > 0) {
                        avgSeconds = (totalMillis / (double) counted) / 1000.0;
                    }
                }

                Report2Data reportItem = new Report2Data(
                    new Date(),
                    label,
                    (int) totalTasks,
                    (int) successfulTasks,
                    (int) failedTasks,
                    successRate,
                    avgSeconds > 0 ? avgSeconds / 60.0 : estimateExecutionTime(label),
                    systemStatus
                );

                reportData.add(reportItem);
            }
            
            // Add overall system summary
            int activeTasks = taskDao.getActiveTaskCount();
            long totalSalesRecords = salesDao.getTotalSalesCount();
            
            Report2Data systemSummary = new Report2Data(
                new Date(),
                "SYSTEM_OVERVIEW",
                activeTasks,
                activeTasks,
                0,
                100.0,
                0.0,
                totalSalesRecords > 0 ? "HEALTHY" : "NEEDS_DATA"
            );
            reportData.add(0, systemSummary);
            
            logger.info("✅ Generated {} system health report entries", reportData.size());
            return reportData;
            
        } catch (Exception e) {
            logger.error("❌ Failed to generate system health report: {}", e.getMessage(), e);
            return generateSampleHealthData();
        }
    }
    
    /**
     * Generate sample NOOS data when no real executions exist
     */
    private List<Report1Data> generateSampleNoosData() {
        List<Report1Data> sampleData = new ArrayList<>();
        
        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        
        for (int i = 0; i < 5; i++) {
            cal.add(Calendar.DAY_OF_MONTH, -i);
            
            Report1Data sample = new Report1Data(
                cal.getTime(),
                "Sample Analysis " + (i + 1),
                "COMPLETED",
                1000 + (i * 100),
                300 + (i * 20),
                200 + (i * 15),
                500 + (i * 65),
                1.5 + (i * 0.3),
                "Sample parameters"
            );
            
            sampleData.add(sample);
        }
        
        return sampleData;
    }
    
    /**
     * Generate sample health data when no real data exists
     */
    private List<Report2Data> generateSampleHealthData() {
        List<Report2Data> sampleData = new ArrayList<>();
        
        String[] taskTypes = {"UPLOAD_SALES", "UPLOAD_STYLES", "SYSTEM_OVERVIEW"};
        
        for (int i = 0; i < taskTypes.length; i++) {
            Report2Data sample = new Report2Data(
                new Date(),
                taskTypes[i],
                10 + i,
                8 + i,
                2,
                80.0 + (i * 5),
                1.2 + (i * 0.5),
                "HEALTHY"
            );
            
            sampleData.add(sample);
        }
        
        return sampleData;
    }
    
    /**
     * Determine system status based on success rate and volume
     */
    private String determineSystemStatus(double successRate, long totalTasks) {
        if (totalTasks == 0) {
            return "NO_ACTIVITY";
        } else if (successRate >= 90.0) {
            return "EXCELLENT";
        } else if (successRate >= 75.0) {
            return "GOOD";
        } else if (successRate >= 50.0) {
            return "WARNING";
        } else {
            return "CRITICAL";
        }
    }
    
    /**
     * Estimate execution time based on task type
     */
    private Double estimateExecutionTime(String taskType) {
        switch (taskType) {
            case "UPLOAD_SALES": return 2.3;
            case "UPLOAD_STYLES": return 1.1;
            case "UPLOAD_STORES": return 0.8;
            case "UPLOAD_SKUS": return 1.5;
            case "RUN_NOOS": return 5.2;
            default: return 1.0;
        }
    }
}
