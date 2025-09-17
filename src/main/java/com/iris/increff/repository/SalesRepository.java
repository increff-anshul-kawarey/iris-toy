package com.iris.increff.repository;

import com.iris.increff.model.Sales;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface SalesRepository extends JpaRepository<Sales, Integer> {
    
    /**
     * Find sales records between start and end dates
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return List of Sales entities within the date range
     */
    List<Sales> findByDateBetween(Date startDate, Date endDate);
    
    /**
     * Find sales records for a specific SKU between start and end dates
     * @param skuId the SKU ID to filter by
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return List of Sales entities for the SKU within the date range
     */
    List<Sales> findBySkuIdAndDateBetween(Integer skuId, Date startDate, Date endDate);
    
    /**
     * Find sales records for a specific store between start and end dates
     * @param storeId the store ID to filter by
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return List of Sales entities for the store within the date range
     */
    List<Sales> findByStoreIdAndDateBetween(Integer storeId, Date startDate, Date endDate);
    
    /**
     * Get sales summary by category for a date range
     * Returns aggregated revenue and quantity grouped by category
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return List of Object arrays containing [category, totalRevenue, totalQuantity]
     */
    @Query("SELECT s.sku.style.category, SUM(s.revenue), SUM(s.quantity) FROM Sales s WHERE s.date BETWEEN :startDate AND :endDate GROUP BY s.sku.style.category")
    List<Object[]> getSalesSummaryByCategory(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
}
