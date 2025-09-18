package com.iris.increff.service;

import com.iris.increff.model.Sales;
import com.iris.increff.model.SalesDTO;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Service interface for managing Sales entities and NOOS algorithm operations.
 * Provides CRUD operations and business logic for sales data analysis.
 */
public interface SalesService {

    /**
     * Saves a new Sales entity from the provided SalesDTO.
     * 
     * @param salesDTO the SalesDTO containing the sales information to be saved
     * @return the saved Sales entity with generated ID
     * @throws IllegalArgumentException if salesDTO is null or contains invalid data
     */
    Sales save(SalesDTO salesDTO);

    /**
     * Retrieves all Sales entities within the specified date range.
     * 
     * @param startDate the start date of the range (inclusive)
     * @param endDate the end date of the range (inclusive)
     * @return a list of Sales entities within the date range, empty list if none found
     * @throws IllegalArgumentException if startDate or endDate is null, or if startDate is after endDate
     */
    List<Sales> findByDateRange(Date startDate, Date endDate);

    /**
     * Retrieves all Sales entities for a specific SKU within the specified date range.
     * 
     * @param skuId the SKU ID to filter sales by
     * @param startDate the start date of the range (inclusive)
     * @param endDate the end date of the range (inclusive)
     * @return a list of Sales entities for the SKU within the date range, empty list if none found
     * @throws IllegalArgumentException if skuId is null, or if startDate or endDate is null, or if startDate is after endDate
     */
    List<Sales> findBySKUAndDateRange(Integer skuId, Date startDate, Date endDate);

    /**
     * Retrieves all Sales entities from the database.
     * 
     * @return a list of all Sales entities, empty list if none found
     */
    List<Sales> findAll();

    /**
     * Updates an existing Sales entity with the provided SalesDTO data.
     * 
     * @param id the unique identifier of the sales record to update
     * @param salesDTO the SalesDTO containing the updated sales information
     * @return the updated Sales entity
     * @throws IllegalArgumentException if id is null or salesDTO is null
     * @throws RuntimeException if sales record with given id is not found
     */
    Sales update(Long id, SalesDTO salesDTO);

    /**
     * Deletes a Sales entity by its unique identifier.
     * 
     * @param id the unique identifier of the sales record to delete
     * @throws IllegalArgumentException if id is null
     * @throws RuntimeException if sales record with given id is not found
     */
    void deleteById(Long id);

    /**
     * Saves multiple Sales entities in a batch operation from a list of SalesDTOs.
     * This method is optimized for bulk insertions and provides better performance
     * compared to individual save operations.
     * 
     * @param salesDTOs the list of SalesDTOs to be saved
     * @return a list of saved Sales entities with generated IDs
     * @throws IllegalArgumentException if salesDTOs is null or empty
     * @throws RuntimeException if any of the SalesDTOs contain invalid data
     */
    List<Sales> saveBatch(List<SalesDTO> salesDTOs);

    // NOOS Algorithm specific methods

    /**
     * Calculates the total revenue for each category within the specified date range.
     * This method is used by the NOOS algorithm to analyze category performance and
     * identify high-performing categories for optimization strategies.
     * 
     * @param startDate the start date of the analysis period (inclusive)
     * @param endDate the end date of the analysis period (inclusive)
     * @return a Map where keys are category names and values are total revenue amounts
     * @throws IllegalArgumentException if startDate or endDate is null, or if startDate is after endDate
     */
    Map<String, Double> getCategoryRevenue(Date startDate, Date endDate);

    /**
     * Generates a comprehensive sales summary grouped by category for the NOOS algorithm.
     * Returns aggregated data including revenue, quantity sold, and other key metrics
     * that are essential for the algorithm's decision-making process.
     * 
     * The returned Object[] array contains:
     * [0] - Category name (String)
     * [1] - Total revenue (Double)
     * [2] - Total quantity sold (Long)
     * [3] - Average revenue per sale (Double)
     * [4] - Number of unique SKUs (Long)
     * [5] - Number of unique stores (Long)
     * 
     * @param startDate the start date of the analysis period (inclusive)
     * @param endDate the end date of the analysis period (inclusive)
     * @return a list of Object arrays containing category-wise sales summary data
     * @throws IllegalArgumentException if startDate or endDate is null, or if startDate is after endDate
     */
    List<Object[]> getSalesSummaryByCategory(Date startDate, Date endDate);

    /**
     * Calculates the total revenue generated by a specific style within the given date range.
     * This method is crucial for the NOOS algorithm to evaluate individual style performance
     * and determine which styles contribute most significantly to overall revenue.
     * 
     * @param styleCode the unique style code to analyze
     * @param startDate the start date of the analysis period (inclusive)
     * @param endDate the end date of the analysis period (inclusive)
     * @return the total revenue generated by the specified style, 0.0 if no sales found
     * @throws IllegalArgumentException if styleCode is null or empty, or if startDate or endDate is null, or if startDate is after endDate
     */
    Double getTotalRevenueByStyle(String styleCode, Date startDate, Date endDate);
}