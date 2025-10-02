package com.iris.increff.controller;

import com.iris.increff.dao.TaskDao;
import com.iris.increff.model.AlgoParametersData;
import com.iris.increff.model.DashBoardData;
import com.iris.increff.model.NoosResult;
import com.iris.increff.model.Task;
import com.iris.increff.service.NoosAlgorithmService;
import com.iris.increff.service.DashboardMetricsService;
import com.iris.increff.exception.ApiException;
import com.iris.increff.util.ProcessTsv;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api
@RestController
public class RunAlgoController {

    private static final Logger logger = LoggerFactory.getLogger(RunAlgoController.class);

    @Autowired
    private NoosAlgorithmService noosAlgorithmService;

    @Autowired
    private TaskDao taskDao;

    @Autowired
    private DashboardMetricsService dashboardMetricsService;

    @ApiOperation(value = "Run NOOS Algorithm (Async)")
    @RequestMapping(path = "/api/run/noos/async", method = RequestMethod.POST)
    @Transactional
    public ResponseEntity<Task> runNoosAlgorithmAsync(@RequestBody AlgoParametersData parameters) {
        System.out.println("🚀 SYSTEM.OUT: Async NOOS Algorithm execution requested with parameters: " + parameters);
        logger.info("🚀 Async NOOS Algorithm execution requested with parameters: {}", parameters);
        
        try {
            // Create task immediately
            Task task = new Task();
            task.setTaskType("ALGORITHM_RUN");
            task.setStatus("PENDING");
            task.setStartTime(new Date());
            task.setUserId("system");
            task.setParameters(formatParameters(parameters));
            task.updateProgress(0.0, "PENDING", "Task created, waiting to start...");
            
            // Save task to get ID
            taskDao.insert(task);
            
            // Start async execution
            try {
                noosAlgorithmService.runNoosAlgorithmAsync(task.getId(), parameters);
                logger.info("✅ Async NOOS algorithm started with task ID: {}", task.getId());
                return ResponseEntity.accepted().body(task); // HTTP 202 Accepted
            } catch (RuntimeException e) {
                // Handle thread pool rejection
                if (e.getMessage().contains("Thread pool queue is full")) {
                    task.setStatus("FAILED");
                    task.setErrorMessage("System is busy. Too many concurrent tasks. Please try again later.");
                    taskDao.update(task);
                    return ResponseEntity.status(429).body(task); // HTTP 429 Too Many Requests
                }
                throw e;
            }
            
        } catch (Exception e) {
            logger.error("❌ Failed to start async NOOS algorithm: {}", e.getMessage(), e);
            
            // Create error response with details
            Task errorTask = new Task();
            errorTask.setTaskType("ALGORITHM_RUN");
            errorTask.setStatus("FAILED");
            errorTask.setErrorMessage("Failed to start async algorithm: " + e.getMessage());
            errorTask.setUserId("system");
            errorTask.setStartTime(new Date());
            errorTask.setEndTime(new Date());
            
            return ResponseEntity.status(500).body(errorTask);
        }
    }

    @ApiOperation(value = "Run NOOS Algorithm (Sync - Legacy)")
    @RequestMapping(path = "/api/run/noos", method = RequestMethod.POST)
    @Transactional
    public ResponseEntity<Task> runNoosAlgorithm(@RequestBody AlgoParametersData parameters) {
        logger.info("🚀 Sync NOOS Algorithm execution requested with parameters: {}", parameters);
        logger.warn("⚠️ Using legacy sync endpoint. Consider using /api/run/noos/async for better performance.");
        
        try {
            Task result = noosAlgorithmService.runNoosAlgorithm(parameters);
            return ResponseEntity.ok(result);
        } catch (ApiException e) {
            logger.error("❌ Failed to run NOOS algorithm: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @ApiOperation(value = "Get NOOS Results")
    @RequestMapping(path = "/api/results/noos", method = RequestMethod.GET)
    public ResponseEntity<List<NoosResult>> getNoosResults() {
        logger.info("📊 Fetching NOOS results");
        
        try {
            List<NoosResult> results = noosAlgorithmService.getLatestResults();
            logger.info("✅ Retrieved {} NOOS results", results.size());
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            logger.error("❌ Failed to fetch NOOS results: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @ApiOperation(value = "Get NOOS Results by Type")
    @RequestMapping(path = "/api/results/noos/{type}", method = RequestMethod.GET)
    public ResponseEntity<List<NoosResult>> getNoosResultsByType(@PathVariable String type) {
        logger.info("📊 Fetching NOOS results for type: {}", type);
        
        try {
            List<NoosResult> results = noosAlgorithmService.getResultsByType(type);
            logger.info("✅ Retrieved {} {} results", results.size(), type);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            logger.error("❌ Failed to fetch NOOS results for type {}: {}", type, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @ApiOperation(value = "Get NOOS Results Summary")
    @RequestMapping(path = "/api/results/noos/summary", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Long>> getNoosResultsSummary() {
        logger.info("📊 Fetching NOOS results summary");
        
        try {
            Map<String, Long> summary = noosAlgorithmService.getResultsCountByType();
            logger.info("✅ Retrieved NOOS summary: {}", summary);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            logger.error("❌ Failed to fetch NOOS summary: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @ApiOperation(value = "Download NOOS Results as TSV")
    @RequestMapping(path = "/api/results/noos/download", method = RequestMethod.GET)
    public void downloadNoosResults(HttpServletResponse response) throws IOException {
        logger.info("📥 NOOS results download requested");
        
        try {
            List<NoosResult> results = noosAlgorithmService.getLatestResults();
            ProcessTsv.createNoosResultsTsv(results, response);
            logger.info("✅ NOOS results download completed: {} results", results.size());
        } catch (Exception e) {
            logger.error("❌ Failed to download NOOS results: {}", e.getMessage(), e);
            throw new IOException("Failed to download NOOS results: " + e.getMessage());
        }
    }

    @ApiOperation(value = "Get NOOS Dashboard Data")
    @RequestMapping(path = "/api/results/noos/dashboard", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getNoosDashboardData() {
        logger.info("📊 Fetching NOOS dashboard data");
        
        try {
            Map<String, Object> dashboardData = new HashMap<>();
            
            // Get results summary
            Map<String, Long> summary = noosAlgorithmService.getResultsCountByType();
            dashboardData.put("summary", summary);
            
            // Get total count
            long totalResults = summary.values().stream().mapToLong(Long::longValue).sum();
            dashboardData.put("totalResults", totalResults);
            
            // Get latest run info
            List<NoosResult> latestResults = noosAlgorithmService.getLatestResults();
            if (!latestResults.isEmpty()) {
                NoosResult latest = latestResults.get(0);
                dashboardData.put("lastRunDate", latest.getCalculatedDate());
                dashboardData.put("hasResults", true);
            } else {
                dashboardData.put("hasResults", false);
            }
            
            // Calculate percentages
            if (totalResults > 0) {
                Map<String, Double> percentages = new HashMap<>();
                for (Map.Entry<String, Long> entry : summary.entrySet()) {
                    double percentage = (entry.getValue() * 100.0) / totalResults;
                    percentages.put(entry.getKey(), Math.round(percentage * 10.0) / 10.0); // Round to 1 decimal
                }
                dashboardData.put("percentages", percentages);
            }
            
            logger.info("✅ Retrieved NOOS dashboard data: {} total results", totalResults);
            return ResponseEntity.ok(dashboardData);
        } catch (Exception e) {
            logger.error("❌ Failed to fetch NOOS dashboard data: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    // Legacy endpoints (keep for backward compatibility)

    @ApiOperation(value = "Run Algo (Legacy)")
    @RequestMapping(path = "/api/run/{algoName}", method = RequestMethod.GET)
    public void updateSales(@PathVariable String algoName) throws ApiException {
        logger.info("🔄 Legacy algorithm endpoint called: {}", algoName);
        
        if ("noos".equalsIgnoreCase(algoName)) {
            logger.info("🔄 Redirecting to new NOOS endpoint");
            // Could redirect to new NOOS endpoint or show deprecation message
        }
        
        System.out.println("Algorithm - " + algoName + " is running");
    }

    @ApiOperation(value = "Execution Updates")
    @RequestMapping(path = "/api/run/updates", method = RequestMethod.GET)
    public DashBoardData executionResults() throws ApiException {
        logger.info("📊 Real dashboard metrics requested");
        System.out.println("Fetching Real Dashboard Tiles Data...");
        try {
            return dashboardMetricsService.getDashboardMetrics();
        } catch (Exception e) {
            logger.error("❌ Failed to get dashboard metrics, falling back to dummy data: {}", e.getMessage());
            System.out.println("❌ Dashboard metrics failed, using fallback data");
            return null;
        }
    }

    @ApiOperation(value = "Execution Updates (legacy endpoint)")
    @RequestMapping(path = "/api/updates", method = RequestMethod.GET)
    public DashBoardData executionResultsLegacy() throws ApiException {
        // Legacy endpoint for dashboard compatibility
        return executionResults();
    }

    /**
     * Format parameters for logging and storage
     */
    private String formatParameters(AlgoParametersData parameters) {
        return String.format("param1=%.2f, param2=%.2f, param3=%.2f, param4=%.2f, param5=%s, startDate=%s, endDate=%s",
                parameters.getLiquidationThreshold(), parameters.getBestsellerMultiplier(),
                parameters.getMinVolumeThreshold(), parameters.getConsistencyThreshold(),
                parameters.getAlgorithmLabel(),
                parameters.getAnalysisStartDate() != null ? parameters.getAnalysisStartDate() : "null",
                parameters.getAnalysisEndDate() != null ? parameters.getAnalysisEndDate() : "null");
    }
}
