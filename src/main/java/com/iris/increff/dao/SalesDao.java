package com.iris.increff.dao;

import com.iris.increff.model.Sales;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Date;
import java.util.List;

/**
 * Data Access Object for Sales entity.
 * Handles database operations using traditional Spring + Hibernate approach.
 * 
 * @author Your Name
 * @version 1.0
 * @since 2025-01-01
 */
@Repository
public class SalesDao {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Save a sales record to the database
     * 
     * @param sales The sales record to save
     * @return The saved sales record with generated ID
     */
    public Sales save(Sales sales) {
        if (sales.getId() == null) {
            entityManager.persist(sales);
        } else {
            entityManager.merge(sales);
        }
        return sales;
    }

    /**
     * Find a sales record by its ID
     * 
     * @param id The sales record ID
     * @return The sales record if found, null otherwise
     */
    public Sales findById(Integer id) {
        return entityManager.find(Sales.class, id);
    }

    /**
     * Find all sales records
     * 
     * @return List of all sales records
     */
    @SuppressWarnings("unchecked")
    public List<Sales> findAll() {
        Query query = entityManager.createQuery("SELECT s FROM Sales s");
        return query.getResultList();
    }

    /**
     * Find sales by date range (useful for NOOS algorithm)
     * 
     * @param startDate Start date for the range
     * @param endDate End date for the range
     * @return List of sales within the date range
     */
    @SuppressWarnings("unchecked")
    public List<Sales> findByDateBetween(Date startDate, Date endDate) {
        Query query = entityManager.createQuery(
            "SELECT s FROM Sales s WHERE s.date BETWEEN :startDate AND :endDate");
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        return query.getResultList();
    }

    /**
     * Find sales for a specific SKU (useful for NOOS algorithm)
     * 
     * @param skuId The SKU ID to search for
     * @return List of sales for the given SKU
     */
    @SuppressWarnings("unchecked")
    public List<Sales> findBySkuId(Integer skuId) {
        Query query = entityManager.createQuery(
            "SELECT s FROM Sales s WHERE s.skuId = :skuId");
        query.setParameter("skuId", skuId);
        return query.getResultList();
    }

    /**
     * Find sales for a specific store (useful for NOOS algorithm)
     * 
     * @param storeId The store ID to search for
     * @return List of sales for the given store
     */
    @SuppressWarnings("unchecked")
    public List<Sales> findByStoreId(Integer storeId) {
        Query query = entityManager.createQuery(
            "SELECT s FROM Sales s WHERE s.storeId = :storeId");
        query.setParameter("storeId", storeId);
        return query.getResultList();
    }

    /**
     * Delete all sales records (for TSV replacement upload)
     */
    public void deleteAll() {
        entityManager.createQuery("DELETE FROM Sales").executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE sales AUTO_INCREMENT = 1").executeUpdate();
        
    }

    /**
     * Save multiple sales records efficiently
     * 
     * @param salesList List of sales records to save
     */
    public void saveAll(List<Sales> salesList) {
        for (int i = 0; i < salesList.size(); i++) {
            entityManager.persist(salesList.get(i));
            if (i % 20 == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        entityManager.flush();
        entityManager.clear();
    }

    /**
     * Get total sales count for NOOS algorithm processing
     * 
     * @return Total number of sales records
     */
    public Long getTotalSalesCount() {
        Query query = entityManager.createQuery("SELECT COUNT(s) FROM Sales s");
        return (Long) query.getSingleResult();
    }
}
