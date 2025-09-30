package com.iris.increff.dao;

import com.iris.increff.model.SKU;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

/**
 * Data Access Object for SKU entity.
 * Handles database operations using traditional Spring + Hibernate approach.
 * 
 * @author Your Name
 * @version 1.0
 * @since 2025-01-01
 */
@Repository
public class SkuDao {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Save a SKU to the database
     * 
     * @param sku The SKU to save
     * @return The saved SKU with generated ID
     */
    public SKU save(SKU sku) {
        if (sku.getId() == null) {
            entityManager.persist(sku);
        } else {
            entityManager.merge(sku);
        }
        return sku;
    }

    /**
     * Find a SKU by its ID
     * 
     * @param id The SKU ID
     * @return The SKU if found, null otherwise
     */
    public SKU findById(Integer id) {
        return entityManager.find(SKU.class, id);
    }

    /**
     * Find a SKU by its SKU code
     * Used for TSV processing to lookup SKU IDs
     * 
     * @param sku The SKU code to search for
     * @return The SKU if found, null otherwise
     */
    public SKU findBySku(String sku) {
        try {
            Query query = entityManager.createQuery(
                "SELECT s FROM SKU s WHERE s.sku = :sku", SKU.class);
            query.setParameter("sku", sku);
            return (SKU) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Find all SKUs
     * 
     * @return List of all SKUs
     */
    @SuppressWarnings("unchecked")
    public List<SKU> findAll() {
        Query query = entityManager.createQuery("SELECT s FROM SKU s");
        return query.getResultList();
    }

    /**
     * Delete all SKUs (for TSV replacement upload)
     */
    public void deleteAll() {
        entityManager.createQuery("DELETE FROM SKU").executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE skus AUTO_INCREMENT = 1").executeUpdate();
    }

    /**
     * Save multiple SKUs efficiently
     * 
     * @param skus List of SKUs to save
     */
    public void saveAll(List<SKU> skus) {
        for (int i = 0; i < skus.size(); i++) {
            entityManager.persist(skus.get(i));
            if (i % 20 == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        entityManager.flush();
        entityManager.clear();
    }

    /**
     * Check if a SKU with given SKU code exists
     * 
     * @param sku The SKU code to check
     * @return true if exists, false otherwise
     */
    public boolean existsBySku(String sku) {
        Query query = entityManager.createQuery(
            "SELECT COUNT(s) FROM SKU s WHERE s.sku = :sku");
        query.setParameter("sku", sku);
        Long count = (Long) query.getSingleResult();
        return count > 0;
    }

    /**
     * Get total SKU count for dashboard metrics
     * 
     * @return Total number of SKU records
     */
    public Long getTotalSkuCount() {
        Query query = entityManager.createQuery("SELECT COUNT(s) FROM SKU s");
        return (Long) query.getSingleResult();
    }
}
