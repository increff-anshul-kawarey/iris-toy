package com.iris.increff.service;

import com.iris.increff.dao.NoosResultDao;
import com.iris.increff.dao.TaskDao;
import com.iris.increff.model.*;
import com.iris.increff.util.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * NOOS (Never Out of Stock) Algorithm Service
 * 
 * PRD Compliance:
 * - Implements liquidation cleanup parameter (remove high discount sales)
 * - Outputs exact format specified in PRD (Category | Style Code | Style ROS | Type | Style Rev Contri)
 * - Uses configurable algorithm parameters from UI
 * - Tracks execution with Task entity for audit trail
 * 
 * Intelligent Enhancements:
 * - Fixes ambiguous PRD formulas with business logic
 * - Adds category-wise benchmarking for better classification
 * - Implements proper error handling and logging
 * - Handles edge cases and data quality issues
 * 
 * Algorithm Logic:
 * - Core: Consistent sellers with low discounts (stable inventory)
 * - Bestseller: High revenue performers (revenue drivers)
 * - Fashion: Everything else (seasonal/trendy items)
 * 
 * @author Anshuk Kawarry
 * @version 1.0
 * @since 2025-01-01
 */
@Service
public class NoosAlgorithmService {

    private static final Logger logger = LoggerFactory.getLogger(NoosAlgorithmService.class);

    // Smart defaults calibrated for your dataset
    private static final double DEFAULT_LIQUIDATION_THRESHOLD = 0.20;
    private static final double DEFAULT_BESTSELLER_MULTIPLIER = 1.5;
    private static final int DEFAULT_MIN_VOLUME = 20;
    private static final double DEFAULT_CONSISTENCY_THRESHOLD = 0.65;
    @Autowired
    private SalesService salesService;

    @Autowired
    private StyleService styleService;

    @Autowired
    private SkuService skuService;

    @Autowired
    private NoosResultDao noosResultDao;

    @Autowired
    private TaskDao taskDao;

    /**
     * Asynchronous NOOS Algorithm Execution
     * 
     * PRD Requirement: "Algorithm Parameters must be editable from UI and algorithms can be run from UI"
     * PRD Requirement: "Maintaining Task and Audit Tables"
     * 
     * @param taskId Task ID for progress tracking
     * @param parameters Algorithm parameters from UI
     * @return CompletableFuture for async execution
     */
    @Async("noosExecutor")
    @Transactional
    public CompletableFuture<Task> runNoosAlgorithmAsync(Long taskId, AlgoParametersData parameters) {
        Task task = taskDao.select(taskId);
        if (task == null) {
            logger.error("❌ Task not found: {}", taskId);
            return CompletableFuture.completedFuture(null);
        }

        System.out.println("🚀 SYSTEM.OUT: Starting async NOOS Algorithm execution for task: " + taskId);
        System.out.println("🔄 SYSTEM.OUT: Thread: " + Thread.currentThread().getName() + ", Task Status: " + task.getStatus());
        logger.info("🚀 Starting async NOOS Algorithm execution for task: {}", taskId);
        logger.info("🔄 Thread: {}, Task Status: {}", Thread.currentThread().getName(), task.getStatus());

        try {
            // Update task to RUNNING status
            task.setStatus("RUNNING");
            task.updateProgress(0.0, "INITIALIZING", "Starting NOOS algorithm...");
            taskDao.update(task);

            // Check for cancellation before each major phase
            if (checkCancellation(task)) {
                return CompletableFuture.completedFuture(task);
            }

            // Phase 1: Data Loading (0% → 20%)
            task.updateProgress(5.0, "DATA_LOADING", "Loading sales data...");
            taskDao.update(task);
            System.out.println("📊 SYSTEM.OUT: Progress 5% - Loading sales data...");
            Thread.sleep(2000); // 2 second delay to see progress

            List<Sales> allSales = getFilteredSales(parameters);
            logger.info("📊 Retrieved {} sales records for analysis", allSales.size());

            if (allSales.isEmpty()) {
                failTask(task, "No sales data available for NOOS algorithm in the specified date range");
                return CompletableFuture.completedFuture(task);
            }

            task.updateProgress(15.0, "DATA_LOADING", String.format("Loaded %d sales records", allSales.size()));
            taskDao.update(task);
            System.out.println("📊 SYSTEM.OUT: Progress 15% - Loaded " + allSales.size() + " sales records");
            Thread.sleep(2000); // 2 second delay to see progress

            // Check for cancellation
            if (checkCancellation(task)) {
                return CompletableFuture.completedFuture(task);
            }

            // Phase 2: Data Processing (20% → 50%)
            task.updateProgress(20.0, "PROCESSING", "Applying liquidation cleanup...");
            taskDao.update(task);
            System.out.println("🧹 SYSTEM.OUT: Progress 20% - Applying liquidation cleanup...");
            Thread.sleep(2000); // 2 second delay to see progress

            double liquidationThreshold = getParameterValue(parameters.getLiquidationThreshold(), DEFAULT_LIQUIDATION_THRESHOLD);
            List<Sales> cleanedSales = applyLiquidationCleanup(allSales, liquidationThreshold);
            logger.info("🧹 After liquidation cleanup ({}%): {} sales records",
                       liquidationThreshold * 100, cleanedSales.size());

            task.updateProgress(35.0, "PROCESSING", String.format("Cleaned data: %d records remaining", cleanedSales.size()));
            taskDao.update(task);

            // Check for cancellation
            if (checkCancellation(task)) {
                return CompletableFuture.completedFuture(task);
            }

            // Aggregate sales by style
            task.updateProgress(40.0, "PROCESSING", "Aggregating sales by style...");
            taskDao.update(task);

            Map<String, StyleSalesData> styleAggregates = aggregateSalesByStyle(cleanedSales);
            logger.info("📈 Aggregated data for {} unique styles", styleAggregates.size());

            task.updateProgress(50.0, "PROCESSING", String.format("Aggregated %d unique styles", styleAggregates.size()));
            taskDao.update(task);

            // Check for cancellation
            if (checkCancellation(task)) {
                return CompletableFuture.completedFuture(task);
            }

            // Phase 3: Classification (50% → 85%)
            task.updateProgress(55.0, "CLASSIFICATION", "Calculating category benchmarks...");
            taskDao.update(task);

            Map<String, CategoryBenchmark> categoryBenchmarks = calculateCategoryBenchmarks(styleAggregates);
            logger.info("🎯 Calculated benchmarks for {} categories", categoryBenchmarks.size());

            // Classify styles with progress tracking
            List<NoosResult> results = new ArrayList<>();
            List<StyleSalesData> styleList = new ArrayList<>(styleAggregates.values());
            int totalStyles = styleList.size();
            int processedStyles = 0;

            task.updateProgress(60.0, "CLASSIFICATION", "Classifying styles...");
            taskDao.update(task);

            for (StyleSalesData styleData : styleList) {
                // Check for cancellation every 50 styles
                if (processedStyles % 50 == 0 && checkCancellation(task)) {
                    return CompletableFuture.completedFuture(task);
                }

                NoosResult result = classifyStyle(styleData, parameters, categoryBenchmarks, taskId);
                results.add(result);
                processedStyles++;

                // Update progress every 50 styles
                if (processedStyles % 50 == 0) {
                    double progress = 60.0 + (25.0 * processedStyles / totalStyles); // 60% → 85%
                    task.updateProgress(progress, "CLASSIFICATION", 
                                      String.format("Classified %d/%d styles", processedStyles, totalStyles));
                    taskDao.update(task);
                }
            }

            task.updateProgress(85.0, "CLASSIFICATION", String.format("Classified all %d styles", totalStyles));
            taskDao.update(task);

            // Check for cancellation
            if (checkCancellation(task)) {
                return CompletableFuture.completedFuture(task);
            }

            // Phase 4: Saving Results (85% → 100%)
            task.updateProgress(90.0, "SAVING", "Saving results to database...");
            taskDao.update(task);

            logger.info("💾 Saving {} NOOS results to database", results.size());
            noosResultDao.deleteAll(); // Clear previous results
            noosResultDao.insertAll(results);

            // Count classifications for reporting
            int coreCount = 0, bestsellerCount = 0, fashionCount = 0;
            for (NoosResult result : results) {
                switch (result.getType()) {
                    case "core": coreCount++; break;
                    case "bestseller": bestsellerCount++; break;
                    case "fashion": fashionCount++; break;
                }
            }

            // Complete task with success
            task.updateProgress(100.0, "COMPLETED", 
                              String.format("Completed: %d Core, %d Bestseller, %d Fashion", 
                                          coreCount, bestsellerCount, fashionCount));
            System.out.println("✅ SYSTEM.OUT: Progress 100% - Completed: " + coreCount + " Core, " + bestsellerCount + " Bestseller, " + fashionCount + " Fashion");
            completeTask(task, results.size(), coreCount, bestsellerCount, fashionCount);

            logger.info("✅ NOOS Algorithm completed successfully!");
            logger.info("📊 Results: {} Core, {} Bestseller, {} Fashion styles",
                       coreCount, bestsellerCount, fashionCount);

        } catch (Exception e) {
            logger.error("❌ NOOS Algorithm failed for task {}: {}", taskId, e.getMessage(), e);
            failTask(task, e.getMessage());
        }

        return CompletableFuture.completedFuture(task);
    }

    /**
     * Synchronous NOOS Algorithm Execution (Legacy - for backward compatibility)
     * 
     * @param parameters Algorithm parameters from UI
     * @return Task entity for tracking execution status
     * @throws ApiException if algorithm execution fails
     */
    @Transactional
    public Task runNoosAlgorithm(AlgoParametersData parameters) throws ApiException {
        logger.info("🚀 Starting NOOS Algorithm execution with parameters: {}", formatParameters(parameters));

        // Create task for tracking (PRD requirement)
        Task task = createAlgorithmTask(parameters);

        try {
            // Step 1: Get all sales data
            List<Sales> allSales = salesService.getAllSales();
            logger.info("📊 Retrieved {} sales records for analysis", allSales.size());
            
            if (allSales.isEmpty()) {
                throw new ApiException("No sales data available for NOOS algorithm");
            }

            // Step 2: Apply liquidation cleanup (PRD mandatory requirement)
            double liquidationThreshold = getParameterValue(parameters.getLiquidationThreshold(), DEFAULT_LIQUIDATION_THRESHOLD);
            List<Sales> cleanedSales = applyLiquidationCleanup(allSales, liquidationThreshold);
            logger.info("🧹 After liquidation cleanup ({}%): {} sales records", 
                       liquidationThreshold * 100, cleanedSales.size());

            // Step 3: Aggregate sales by style for analysis
            Map<String, StyleSalesData> styleAggregates = aggregateSalesByStyle(cleanedSales);
            logger.info("📈 Aggregated data for {} unique styles", styleAggregates.size());

            // Step 4: Calculate category benchmarks for intelligent classification
            Map<String, CategoryBenchmark> categoryBenchmarks = calculateCategoryBenchmarks(styleAggregates);
            logger.info("🎯 Calculated benchmarks for {} categories", categoryBenchmarks.size());

            // Step 5: Classify each style and create results
            List<NoosResult> results = new ArrayList<>();
            int coreCount = 0, bestsellerCount = 0, fashionCount = 0;

            for (StyleSalesData styleData : styleAggregates.values()) {
                NoosResult result = classifyStyle(styleData, parameters, categoryBenchmarks, task.getId());
                results.add(result);

                // Count classifications for reporting
                switch (result.getType()) {
                    case "core": coreCount++; break;
                    case "bestseller": bestsellerCount++; break;
                    case "fashion": fashionCount++; break;
                }
            }

            // Step 6: Save results to database
            logger.info("💾 Saving {} NOOS results to database", results.size());
            noosResultDao.deleteAll(); // Clear previous results
            noosResultDao.insertAll(results);

            // Step 7: Update task with success status
            completeTask(task, results.size(), coreCount, bestsellerCount, fashionCount);
            
            logger.info("✅ NOOS Algorithm completed successfully!");
            logger.info("📊 Results: {} Core, {} Bestseller, {} Fashion styles", 
                       coreCount, bestsellerCount, fashionCount);

        } catch (Exception e) {
            logger.error("❌ NOOS Algorithm failed: {}", e.getMessage(), e);
            failTask(task, e.getMessage());
            throw new ApiException("NOOS Algorithm execution failed: " + e.getMessage());
        }

        return task;
    }

    /**
     * PRD Requirement: "Liquidation clean up parameter"
     * "Remove sales with more than X% discount"
     * 
     * @param allSales All sales data
     * @param discountThreshold Maximum allowed discount percentage (0.20 = 20%)
     * @return Cleaned sales data with high-discount sales removed
     */
    private List<Sales> applyLiquidationCleanup(List<Sales> allSales, double discountThreshold) {
        logger.debug("🧹 Applying liquidation cleanup with threshold: {}%", discountThreshold * 100);

        return allSales.stream()
                .filter(sale -> {
                    if (sale.getRevenue().compareTo(BigDecimal.ZERO) <= 0) {
                        return false; // Skip zero/negative revenue sales
                    }
                    
                    // Calculate discount percentage: discount / (discount + revenue)
                    double totalValue = sale.getDiscount().doubleValue() + sale.getRevenue().doubleValue();
                    double discountPercentage = sale.getDiscount().doubleValue() / totalValue;
                    
                    return discountPercentage <= discountThreshold;
                })
                .collect(Collectors.toList());
    }

    /**
     * Aggregate sales data by style for analysis
     * Maps SKU-level sales to Style-level aggregates
     * 
     * @param cleanedSales Sales data after liquidation cleanup
     * @return Map of StyleCode -> StyleSalesData
     * @throws ApiException if data mapping fails
     */
    private Map<String, StyleSalesData> aggregateSalesByStyle(List<Sales> cleanedSales) throws ApiException {
        Map<String, StyleSalesData> styleMap = new HashMap<>();
        
        // Get all SKUs and Styles for mapping
        List<SKU> allSkus = skuService.getAllSKUs();
        List<Style> allStyles = styleService.getAllStyles();
        
        // Create lookup maps for performance
        Map<Integer, SKU> skuMap = allSkus.stream()
                .collect(Collectors.toMap(SKU::getId, sku -> sku));
        Map<Integer, Style> styleMap2 = allStyles.stream()
                .collect(Collectors.toMap(Style::getId, style -> style));

        logger.debug("📊 Processing {} sales records across {} SKUs and {} styles", 
                    cleanedSales.size(), allSkus.size(), allStyles.size());

        for (Sales sale : cleanedSales) {
            // Map Sale -> SKU -> Style
            SKU sku = skuMap.get(sale.getSkuId());
            if (sku == null) {
                logger.warn("⚠️ SKU not found for sale ID: {}, skipping", sale.getId());
                continue;
            }

            Style style = styleMap2.get(sku.getStyleId());
            if (style == null) {
                logger.warn("⚠️ Style not found for SKU: {}, skipping", sku.getId());
                continue;
            }

            // Get or create StyleSalesData
            String styleCode = style.getStyleCode();
            StyleSalesData styleData = styleMap.computeIfAbsent(styleCode, 
                k -> new StyleSalesData(styleCode, style.getCategory(), style.getGender()));

            // Aggregate the sales data
            styleData.addSale(sale);
        }

        logger.debug("📈 Successfully aggregated {} styles from sales data", styleMap.size());
        return styleMap;
    }

    /**
     * Calculate category-wise benchmarks for intelligent classification
     * This enables comparing styles against their category peers rather than global averages
     * 
     * @param styleAggregates Map of all style sales data
     * @return Map of Category -> CategoryBenchmark
     */
    private Map<String, CategoryBenchmark> calculateCategoryBenchmarks(Map<String, StyleSalesData> styleAggregates) {
        Map<String, CategoryBenchmark> benchmarks = new HashMap<>();
        
        // Group styles by category
        Map<String, List<StyleSalesData>> stylesByCategory = styleAggregates.values().stream()
                .collect(Collectors.groupingBy(StyleSalesData::getCategory));

        for (Map.Entry<String, List<StyleSalesData>> entry : stylesByCategory.entrySet()) {
            String category = entry.getKey();
            List<StyleSalesData> categoryStyles = entry.getValue();

            // Calculate category benchmarks
            double totalRevenue = categoryStyles.stream()
                    .mapToDouble(StyleSalesData::getTotalRevenue)
                    .sum();

            double avgRevenuePerDay = categoryStyles.stream()
                    .mapToDouble(s -> s.getTotalRevenue() / Math.max(s.getDaysAvailable(), 1))
                    .average()
                    .orElse(0.0);

            double avgConsistency = categoryStyles.stream()
                    .mapToDouble(s -> (double) s.getDaysWithSales() / Math.max(s.getDaysAvailable(), 1))
                    .average()
                    .orElse(0.0);

            CategoryBenchmark benchmark = new CategoryBenchmark(category, totalRevenue, avgRevenuePerDay, avgConsistency);
            benchmarks.put(category, benchmark);

            logger.debug("🎯 Category {}: {} styles, ${:.2f} total revenue, ${:.2f} avg revenue/day, {:.1f}% avg consistency",
                        category, categoryStyles.size(), totalRevenue, avgRevenuePerDay, avgConsistency * 100);
        }

        return benchmarks;
    }

    /**
     * Classify individual style based on PRD requirements and intelligent business logic
     * 
     * PRD Output Format: Category | Style Code | Style ROS | Type | Style Rev Contri
     * 
     * @param styleData Aggregated sales data for the style
     * @param parameters Algorithm parameters from UI
     * @param categoryBenchmarks Benchmarks for intelligent classification
     * @param taskId Task ID for audit trail
     * @return NoosResult with classification and metrics
     */
    private NoosResult classifyStyle(StyleSalesData styleData, AlgoParametersData parameters, 
                                   Map<String, CategoryBenchmark> categoryBenchmarks, Long taskId) {
        NoosResult result = new NoosResult();
        result.setCategory(styleData.getCategory());
        result.setStyleCode(styleData.getStyleCode());
        result.setCalculatedDate(new Date());
        result.setAlgorithmRunId(taskId);

        // Calculate Style ROS (PRD formula interpretation)
        // PRD: "Style ROS = (No. of styles sold) / (No. of days for the category)"
        // Intelligent interpretation: Total Quantity / Days Style Available
        BigDecimal styleROS = BigDecimal.ZERO;
        if (styleData.getDaysAvailable() > 0) {
            styleROS = BigDecimal.valueOf(styleData.getTotalQuantity())
                    .divide(BigDecimal.valueOf(styleData.getDaysAvailable()), 4, RoundingMode.HALF_UP);
        }
        result.setStyleROS(styleROS);

        // Calculate Revenue Contribution (PRD requirement)
        // Style Rev Contribution = (Style Revenue / Category Revenue) * 100
        CategoryBenchmark categoryBenchmark = categoryBenchmarks.get(styleData.getCategory());
        BigDecimal revContribution = BigDecimal.ZERO;
        if (categoryBenchmark != null && categoryBenchmark.getTotalRevenue() > 0) {
            revContribution = BigDecimal.valueOf(styleData.getTotalRevenue() / categoryBenchmark.getTotalRevenue() * 100)
                    .setScale(4, RoundingMode.HALF_UP);
        }
        result.setStyleRevContribution(revContribution);

        // Set additional metrics for analysis
        result.setTotalQuantitySold(styleData.getTotalQuantity());
        result.setTotalRevenue(BigDecimal.valueOf(styleData.getTotalRevenue()));
        result.setDaysAvailable(styleData.getDaysAvailable());
        result.setDaysWithSales(styleData.getDaysWithSales());
        result.setAvgDiscount(BigDecimal.valueOf(styleData.getAvgDiscount()).setScale(4, RoundingMode.HALF_UP));

        // Classification logic using algorithm parameters
        String type = determineStyleType(styleData, parameters, categoryBenchmark);
        result.setType(type);

        return result;
    }

    /**
     * Determine style type based on intelligent business logic and algorithm parameters
     * 
     * Classification Rules:
     * - Bestseller: High revenue performers (revenue drivers)
     * - Core: Consistent sellers with low discounts (stable inventory)
     * - Fashion: Everything else (seasonal/trendy items)
     * 
     * @param styleData Style sales data
     * @param parameters Algorithm parameters from UI
     * @param categoryBenchmark Category benchmarks for comparison
     * @return Style type: "core", "bestseller", or "fashion"
     */
    private String determineStyleType(StyleSalesData styleData, AlgoParametersData parameters, 
                                    CategoryBenchmark categoryBenchmark) {
        // Extract parameters with defaults
        double bestsellerMultiplier = getParameterValue(parameters.getBestsellerMultiplier(), DEFAULT_BESTSELLER_MULTIPLIER);
        int minVolume = (int) getParameterValue(parameters.getMinVolumeThreshold(), DEFAULT_MIN_VOLUME);
        double consistencyThreshold = getParameterValue(parameters.getConsistencyThreshold(), DEFAULT_CONSISTENCY_THRESHOLD);

        // Calculate key metrics
        double revenuePerDay = styleData.getTotalRevenue() / Math.max(styleData.getDaysAvailable(), 1);
        double consistencyRatio = (double) styleData.getDaysWithSales() / Math.max(styleData.getDaysAvailable(), 1);
        double avgDiscount = styleData.getAvgDiscount();

        // Get category benchmarks (fallback to defaults if not available)
        double categoryAvgRevenue = categoryBenchmark != null ? categoryBenchmark.getAvgRevenuePerDay() : 100.0;

        // Classification logic
        
        // Rule 1: Bestseller - High revenue performers
        if (revenuePerDay > (categoryAvgRevenue * bestsellerMultiplier) && 
            styleData.getTotalQuantity() > minVolume) {
            logger.debug("🌟 {} classified as BESTSELLER: revenue/day=${:.2f} ({}x category avg), qty={}", 
                        styleData.getStyleCode(), revenuePerDay, bestsellerMultiplier, styleData.getTotalQuantity());
            return "bestseller";
        }
        
        // Rule 2: Core - Consistent, low-discount sellers
        else if (consistencyRatio > consistencyThreshold && 
                 avgDiscount < 0.15 && 
                 styleData.getTotalQuantity() > (minVolume / 2)) { // Lower volume threshold for core
            logger.debug("🎯 {} classified as CORE: consistency={:.1f}%, discount={:.1f}%, qty={}", 
                        styleData.getStyleCode(), consistencyRatio * 100, avgDiscount * 100, styleData.getTotalQuantity());
            return "core";
        }
        
        // Rule 3: Fashion - Everything else
        else {
            logger.debug("👗 {} classified as FASHION: revenue/day=${:.2f}, consistency={:.1f}%, discount={:.1f}%", 
                        styleData.getStyleCode(), revenuePerDay, consistencyRatio * 100, avgDiscount * 100);
            return "fashion";
        }
    }

    /**
     * Get parameter value with fallback to default
     */
    private double getParameterValue(double paramValue, double defaultValue) {
        return paramValue > 0 ? paramValue : defaultValue;
    }

    /**
     * Check if task cancellation was requested
     * 
     * @param task Task to check
     * @return true if cancellation was requested
     */
    private boolean checkCancellation(Task task) {
        // Refresh task from database to get latest cancellation status
        Task refreshedTask = taskDao.select(task.getId());
        if (refreshedTask != null && refreshedTask.isCancellationRequested()) {
            logger.info("🛑 Cancellation detected for task: {}", task.getId());
            task.setStatus("CANCELLED");
            task.updateProgress(task.getProgressPercentage(), "CANCELLED", "Task was cancelled by user");
            taskDao.update(task);
            return true;
        }
        return false;
    }

    /**
     * Create algorithm execution task for tracking
     */
    private Task createAlgorithmTask(AlgoParametersData parameters) {
        Task task = new Task();
        task.setTaskType("ALGORITHM_RUN");
        task.setStatus("RUNNING");
        task.setStartTime(new Date());
        task.setUserId("system"); // Could be enhanced to track actual user
        task.setParameters(formatParameters(parameters));
        
        taskDao.insert(task);
        logger.info("📋 Created algorithm task with ID: {}", task.getId());
        return task;
    }

    /**
     * Complete task with success status and metrics
     */
    private void completeTask(Task task, int totalStyles, int coreCount, int bestsellerCount, int fashionCount) {
        task.setStatus("COMPLETED");
        task.setEndTime(new Date());
        task.setTotalRecords(totalStyles);
        task.setProcessedRecords(totalStyles);
        task.setErrorCount(0);
        
        // Add classification summary to parameters
        String summary = String.format("Core: %d, Bestseller: %d, Fashion: %d", coreCount, bestsellerCount, fashionCount);
        task.setParameters(task.getParameters() + ", Results: " + summary);
        
        taskDao.update(task);
        logger.info("✅ Task {} completed successfully", task.getId());
    }

    /**
     * Mark task as failed with error message
     */
    private void failTask(Task task, String errorMessage) {
        task.setStatus("FAILED");
        task.setEndTime(new Date());
        task.setErrorMessage(errorMessage);
        taskDao.update(task);
        logger.error("❌ Task {} failed: {}", task.getId(), errorMessage);
    }

    /**
     * Get filtered sales data based on algorithm parameters
     * Uses date range filtering if specified, otherwise returns all sales
     *
     * @param parameters Algorithm parameters containing date range
     * @return Filtered list of sales data
     */
    private List<Sales> getFilteredSales(AlgoParametersData parameters) {
        if (parameters.getAnalysisStartDate() != null && parameters.getAnalysisEndDate() != null) {
            // Use date range filtering
            return salesService.getSalesByDateRange(
                parameters.getAnalysisStartDate(),
                parameters.getAnalysisEndDate()
            );
        } else {
            // Fallback to all sales if no date range specified
            logger.warn("⚠️ No date range specified, using all sales data");
            return salesService.getAllSales();
        }
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

    /**
     * Get latest NOOS results for display
     */
    public List<NoosResult> getLatestResults() {
        return noosResultDao.getLatestResults();
    }

    /**
     * Get NOOS results by type for analysis
     */
    public List<NoosResult> getResultsByType(String type) {
        return noosResultDao.getResultsByType(type);
    }

    /**
     * Get NOOS results count by type for dashboard
     */
    public Map<String, Long> getResultsCountByType() {
        Map<String, Long> counts = new HashMap<>();
        counts.put("core", noosResultDao.getCountByType("core"));
        counts.put("bestseller", noosResultDao.getCountByType("bestseller"));
        counts.put("fashion", noosResultDao.getCountByType("fashion"));
        return counts;
    }

    // Helper classes for internal processing

    /**
     * Internal class for aggregating style sales data
     */
    private static class StyleSalesData {
        private String styleCode;
        private String category;
        private String gender;
        private int totalQuantity = 0;
        private double totalRevenue = 0.0;
        private double totalDiscount = 0.0;
        private Set<String> salesDates = new HashSet<>();

        public StyleSalesData(String styleCode, String category, String gender) {
            this.styleCode = styleCode;
            this.category = category;
            this.gender = gender;
        }

        public void addSale(Sales sale) {
            totalQuantity += sale.getQuantity();
            totalRevenue += sale.getRevenue().doubleValue();
            totalDiscount += sale.getDiscount().doubleValue();
            
            // Track unique sales dates for consistency calculation
            salesDates.add(sale.getDate().toString());
        }

        // Getters
        public String getStyleCode() { return styleCode; }
        public String getCategory() { return category; }
        public int getTotalQuantity() { return totalQuantity; }
        public double getTotalRevenue() { return totalRevenue; }
        public int getDaysWithSales() { return salesDates.size(); }
        public int getDaysAvailable() { 
            // For now, assume available for all unique sales dates
            // In real implementation, this would be calculated from availability data
            return Math.max(salesDates.size(), 1); 
        }
        public double getAvgDiscount() { 
            double totalValue = totalDiscount + totalRevenue;
            return totalValue > 0 ? totalDiscount / totalValue : 0.0; 
        }
    }

    /**
     * Internal class for category benchmarks
     */
    private static class CategoryBenchmark {
        private String category;
        private double totalRevenue;
        private double avgRevenuePerDay;
        private double avgConsistency;

        public CategoryBenchmark(String category, double totalRevenue, double avgRevenuePerDay, double avgConsistency) {
            this.category = category;
            this.totalRevenue = totalRevenue;
            this.avgRevenuePerDay = avgRevenuePerDay;
            this.avgConsistency = avgConsistency;
        }

        // Getters
        public String getCategory() { return category; }
        public double getTotalRevenue() { return totalRevenue; }
        public double getAvgRevenuePerDay() { return avgRevenuePerDay; }
        public double getAvgConsistency() { return avgConsistency; }
    }
}
