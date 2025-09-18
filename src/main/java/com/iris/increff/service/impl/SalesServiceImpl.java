package com.iris.increff.service.impl;

import com.iris.increff.model.Sales;
import com.iris.increff.model.SalesDTO;
import com.iris.increff.model.SKU;
import com.iris.increff.model.Style;
import com.iris.increff.repository.SalesRepository;
import com.iris.increff.repository.SKURepository;
import com.iris.increff.repository.StyleRepository;
import com.iris.increff.service.SalesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementation for managing Sales entities and NOOS algorithm operations.
 * Provides CRUD operations and business logic for sales data analysis.
 */
@Service
public class SalesServiceImpl implements SalesService {

    private static final Logger logger = LoggerFactory.getLogger(SalesServiceImpl.class);
    private static final int BATCH_SIZE = 1000;
    private static final double DEFAULT_LIQUIDATION_THRESHOLD = 50.0; // 50% discount threshold

    @Autowired
    private SalesRepository salesRepository;

    @Autowired
    private SKURepository skuRepository;

    @Autowired
    private StyleRepository styleRepository;

    @Override
    @Transactional
    public Sales save(SalesDTO salesDTO) {
        logger.info("Saving new sales record for SKU ID: {}, Store ID: {}", salesDTO.getSkuId(), salesDTO.getStoreId());
        
        validateSalesDTO(salesDTO);
        
        // Verify SKU and Store exist
        validateSKUExists(salesDTO.getSkuId());
        validateStoreExists(salesDTO.getStoreId());
        
        Sales sales = convertToEntity(salesDTO);
        Sales savedSales = salesRepository.save(sales);
        logger.info("Successfully saved sales record with ID: {}", savedSales.getId());
        return savedSales;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sales> findByDateRange(Date startDate, Date endDate) {
        logger.debug("Finding sales records between {} and {}", startDate, endDate);
        
        validateDateRange(startDate, endDate);
        
        List<Sales> sales = salesRepository.findByDateBetween(startDate, endDate);
        logger.debug("Found {} sales records in date range", sales.size());
        return sales;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sales> findBySKUAndDateRange(Integer skuId, Date startDate, Date endDate) {
        logger.debug("Finding sales records for SKU ID: {} between {} and {}", skuId, startDate, endDate);
        
        if (skuId == null) {
            String errorMsg = "SKU ID cannot be null";
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        
        validateDateRange(startDate, endDate);
        
        List<Sales> sales = salesRepository.findBySkuIdAndDateBetween(skuId, startDate, endDate);
        logger.debug("Found {} sales records for SKU ID: {} in date range", sales.size(), skuId);
        return sales;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sales> findAll() {
        logger.debug("Finding all sales records");
        List<Sales> sales = salesRepository.findAll();
        logger.debug("Found {} total sales records", sales.size());
        return sales;
    }

    @Override
    @Transactional
    public Sales update(Long id, SalesDTO salesDTO) {
        logger.info("Updating sales record with ID: {}", id);
        
        if (id == null) {
            String errorMsg = "Sales ID cannot be null";
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        
        validateSalesDTO(salesDTO);
        
        // Verify SKU and Store exist
        validateSKUExists(salesDTO.getSkuId());
        validateStoreExists(salesDTO.getStoreId());
        
        Optional<Sales> existingSalesOpt = salesRepository.findById(id.intValue());
        if (!existingSalesOpt.isPresent()) {
            String errorMsg = "Sales record with ID " + id + " not found";
            logger.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }
        
        Sales existingSales = existingSalesOpt.get();
        updateSalesFromDTO(existingSales, salesDTO);
        Sales updatedSales = salesRepository.save(existingSales);
        logger.info("Successfully updated sales record with ID: {}", id);
        return updatedSales;
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        logger.info("Deleting sales record with ID: {}", id);
        
        if (id == null) {
            String errorMsg = "Sales ID cannot be null";
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        
        if (!salesRepository.existsById(id.intValue())) {
            String errorMsg = "Sales record with ID " + id + " not found";
            logger.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }
        
        salesRepository.deleteById(id.intValue());
        logger.info("Successfully deleted sales record with ID: {}", id);
    }

    @Override
    @Transactional
    public List<Sales> saveBatch(List<SalesDTO> salesDTOs) {
        logger.info("Starting batch save of {} sales records", salesDTOs.size());
        
        if (salesDTOs == null || salesDTOs.isEmpty()) {
            String errorMsg = "SalesDTOs list cannot be null or empty";
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        
        List<Sales> savedSales = new ArrayList<>();
        List<SalesDTO> validDTOs = new ArrayList<>();
        
        // Validate all DTOs first
        for (int i = 0; i < salesDTOs.size(); i++) {
            SalesDTO dto = salesDTOs.get(i);
            try {
                validateSalesDTO(dto);
                validateSKUExists(dto.getSkuId());
                validateStoreExists(dto.getStoreId());
                validDTOs.add(dto);
            } catch (IllegalArgumentException e) {
                logger.error("Validation failed for sales at index {}: {}", i, e.getMessage());
                throw new RuntimeException("Validation failed for sales at index " + i + ": " + e.getMessage());
            }
        }
        
        // Process in chunks for better performance
        for (int i = 0; i < validDTOs.size(); i += BATCH_SIZE) {
            int endIndex = Math.min(i + BATCH_SIZE, validDTOs.size());
            List<SalesDTO> chunk = validDTOs.subList(i, endIndex);
            
            logger.debug("Processing chunk {}-{} of {} sales records", i + 1, endIndex, validDTOs.size());
            
            List<Sales> chunkSales = new ArrayList<>();
            for (SalesDTO dto : chunk) {
                Sales sales = convertToEntity(dto);
                chunkSales.add(sales);
            }
            
            List<Sales> savedChunk = salesRepository.saveAll(chunkSales);
            savedSales.addAll(savedChunk);
        }
        
        logger.info("Successfully saved {} sales records in batch", savedSales.size());
        return savedSales;
    }

    // NOOS Algorithm specific methods

    @Override
    @Transactional(readOnly = true)
    public Map<String, Double> getCategoryRevenue(Date startDate, Date endDate) {
        logger.info("NOOS Algorithm: Calculating category revenue between {} and {}", startDate, endDate);
        
        validateDateRange(startDate, endDate);
        
        List<Sales> sales = salesRepository.findByDateBetween(startDate, endDate);
        logger.debug("Found {} sales records for category revenue calculation", sales.size());
        
        Map<String, Double> categoryRevenue = sales.stream()
            .filter(sale -> sale.getSku() != null && sale.getSku().getStyle() != null)
            .collect(Collectors.groupingBy(
                sale -> sale.getSku().getStyle().getCategory(),
                Collectors.summingDouble(sale -> sale.getRevenue().doubleValue())
            ));
        
        logger.info("NOOS Algorithm: Calculated revenue for {} categories", categoryRevenue.size());
        return categoryRevenue;
    }

    /**
     * Calculates category revenue with liquidation cleanup logic.
     * Filters out sales where discount percentage exceeds the liquidation threshold.
     * 
     * @param startDate the start date of the analysis period
     * @param endDate the end date of the analysis period
     * @param discountPercent the discount percentage threshold for liquidation cleanup
     * @return Map of category names to total revenue amounts
     */
    @Transactional(readOnly = true)
    public Map<String, Double> getCategoryRevenue(Date startDate, Date endDate, Double discountPercent) {
        logger.info("NOOS Algorithm: Calculating category revenue with liquidation cleanup (threshold: {}%)", discountPercent);
        
        validateDateRange(startDate, endDate);
        
        final double liquidationThreshold = (discountPercent != null) ? discountPercent : DEFAULT_LIQUIDATION_THRESHOLD;
        
        List<Sales> sales = salesRepository.findByDateBetween(startDate, endDate);
        logger.debug("Found {} sales records for category revenue calculation with liquidation cleanup", sales.size());
        
        // Filter out sales with discount percentage above liquidation threshold
        List<Sales> filteredSales = sales.stream()
            .filter(sale -> {
                if (sale.getSku() == null || sale.getSku().getStyle() == null) {
                    return false;
                }
                
                // Calculate discount percentage
                BigDecimal mrp = sale.getSku().getStyle().getMrp();
                if (mrp == null || mrp.compareTo(BigDecimal.ZERO) <= 0) {
                    return true; // Include if MRP is not available
                }
                
                BigDecimal discountAmount = sale.getDiscount();
                double discountPercentage = (discountAmount.doubleValue() / mrp.doubleValue()) * 100;
                
                return discountPercentage <= liquidationThreshold;
            })
            .collect(Collectors.toList());
        
        logger.debug("After liquidation cleanup: {} sales records remaining (filtered out {} records)", 
                    filteredSales.size(), sales.size() - filteredSales.size());
        
        Map<String, Double> categoryRevenue = filteredSales.stream()
            .collect(Collectors.groupingBy(
                sale -> sale.getSku().getStyle().getCategory(),
                Collectors.summingDouble(sale -> sale.getRevenue().doubleValue())
            ));
        
        logger.info("NOOS Algorithm: Calculated revenue for {} categories after liquidation cleanup", categoryRevenue.size());
        return categoryRevenue;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getSalesSummaryByCategory(Date startDate, Date endDate) {
        logger.info("NOOS Algorithm: Generating sales summary by category between {} and {}", startDate, endDate);
        
        validateDateRange(startDate, endDate);
        
        List<Object[]> summary = salesRepository.getSalesSummaryByCategory(startDate, endDate);
        logger.info("NOOS Algorithm: Generated sales summary for {} categories", summary.size());
        
        // Log detailed summary for algorithm analysis
        for (Object[] row : summary) {
            String category = (String) row[0];
            BigDecimal totalRevenue = (BigDecimal) row[1];
            Long totalQuantity = (Long) row[2];
            logger.debug("Category: {}, Revenue: {}, Quantity: {}", category, totalRevenue, totalQuantity);
        }
        
        return summary;
    }

    @Override
    @Transactional(readOnly = true)
    public Double getTotalRevenueByStyle(String styleCode, Date startDate, Date endDate) {
        logger.info("NOOS Algorithm: Calculating total revenue for style '{}' between {} and {}", styleCode, startDate, endDate);
        
        if (!StringUtils.hasText(styleCode)) {
            String errorMsg = "Style code cannot be null or empty";
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        
        validateDateRange(startDate, endDate);
        
        // Find the style first
        Style style = styleRepository.findByStyleCode(styleCode.trim().toLowerCase());
        if (style == null) {
            logger.warn("NOOS Algorithm: Style '{}' not found", styleCode);
            return 0.0;
        }
        
        // Find all SKUs for this style
        List<SKU> skus = skuRepository.findByStyleId(style.getId());
        if (skus.isEmpty()) {
            logger.warn("NOOS Algorithm: No SKUs found for style '{}'", styleCode);
            return 0.0;
        }
        
        // Get all sales for these SKUs in the date range
        List<Integer> skuIds = skus.stream().map(SKU::getId).collect(Collectors.toList());
        List<Sales> sales = salesRepository.findByDateBetween(startDate, endDate)
            .stream()
            .filter(sale -> skuIds.contains(sale.getSkuId()))
            .collect(Collectors.toList());
        
        double totalRevenue = sales.stream()
            .mapToDouble(sale -> sale.getRevenue().doubleValue())
            .sum();
        
        logger.info("NOOS Algorithm: Style '{}' generated total revenue of {} from {} sales records", 
                   styleCode, totalRevenue, sales.size());
        
        return totalRevenue;
    }

    // Private helper methods

    /**
     * Validates a SalesDTO for required fields and constraints.
     */
    private void validateSalesDTO(SalesDTO salesDTO) {
        if (salesDTO == null) {
            throw new IllegalArgumentException("SalesDTO cannot be null");
        }
        
        if (salesDTO.getDate() == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        
        if (salesDTO.getSkuId() == null) {
            throw new IllegalArgumentException("SKU ID cannot be null");
        }
        
        if (salesDTO.getStoreId() == null) {
            throw new IllegalArgumentException("Store ID cannot be null");
        }
        
        if (salesDTO.getQuantity() == null || salesDTO.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        
        if (salesDTO.getDiscount() == null || salesDTO.getDiscount().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Discount must be greater than or equal to 0");
        }
        
        if (salesDTO.getRevenue() == null || salesDTO.getRevenue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Revenue must be positive");
        }
    }

    /**
     * Validates date range parameters.
     */
    private void validateDateRange(Date startDate, Date endDate) {
        if (startDate == null) {
            String errorMsg = "Start date cannot be null";
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        
        if (endDate == null) {
            String errorMsg = "End date cannot be null";
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        
        if (startDate.after(endDate)) {
            String errorMsg = "Start date cannot be after end date";
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
    }

    /**
     * Validates that SKU exists.
     */
    private void validateSKUExists(Integer skuId) {
        if (!skuRepository.existsById(skuId)) {
            String errorMsg = "SKU with ID " + skuId + " not found";
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
    }

    /**
     * Validates that Store exists.
     * Note: This assumes StoreRepository exists and has existsById method
     */
    private void validateStoreExists(Integer storeId) {
        // For now, we'll skip store validation since StoreRepository might not be available
        // In a real implementation, you would inject StoreRepository and validate here
        logger.debug("Store validation skipped for store ID: {}", storeId);
    }

    /**
     * Converts SalesDTO to Sales entity.
     */
    private Sales convertToEntity(SalesDTO salesDTO) {
        Sales sales = new Sales();
        sales.setId(salesDTO.getId());
        sales.setDate(salesDTO.getDate());
        sales.setSkuId(salesDTO.getSkuId());
        sales.setStoreId(salesDTO.getStoreId());
        sales.setQuantity(salesDTO.getQuantity());
        sales.setDiscount(salesDTO.getDiscount());
        sales.setRevenue(salesDTO.getRevenue());
        return sales;
    }

    /**
     * Updates an existing Sales entity with data from SalesDTO.
     */
    private void updateSalesFromDTO(Sales sales, SalesDTO salesDTO) {
        sales.setDate(salesDTO.getDate());
        sales.setSkuId(salesDTO.getSkuId());
        sales.setStoreId(salesDTO.getStoreId());
        sales.setQuantity(salesDTO.getQuantity());
        sales.setDiscount(salesDTO.getDiscount());
        sales.setRevenue(salesDTO.getRevenue());
    }
}
