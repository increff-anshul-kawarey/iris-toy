package com.iris.increff.dao;

import com.iris.increff.model.Store;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

/**
 * Data Access Object for Store entity.
 * Handles database operations using traditional Spring + Hibernate approach.
 * 
 * @author Your Name
 * @version 1.0
 * @since 2025-01-01
 */
@Repository
public class StoreDao {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Save a store to the database
     * 
     * @param store The store to save
     * @return The saved store with generated ID
     */
    public Store save(Store store) {
        if (store.getId() == null) {
            entityManager.persist(store);
        } else {
            entityManager.merge(store);
        }
        return store;
    }

    /**
     * Find a store by its ID
     * 
     * @param id The store ID
     * @return The store if found, null otherwise
     */
    public Store findById(Integer id) {
        return entityManager.find(Store.class, id);
    }

    /**
     * Find a store by its branch name
     * Used for TSV processing to lookup store IDs from channel names
     * 
     * @param branch The branch name to search for
     * @return The store if found, null otherwise
     */
    public Store findByBranch(String branch) {
        try {
            Query query = entityManager.createQuery(
                "SELECT s FROM Store s WHERE s.branch = :branch", Store.class);
            query.setParameter("branch", branch);
            return (Store) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Find all stores
     * 
     * @return List of all stores
     */
    @SuppressWarnings("unchecked")
    public List<Store> findAll() {
        Query query = entityManager.createQuery("SELECT s FROM Store s");
        return query.getResultList();
    }

    /**
     * Delete all stores (for TSV replacement upload)
     */
    public void deleteAll() {
        entityManager.createQuery("DELETE FROM Store").executeUpdate();
        // Skip auto-increment reset - not essential for functionality
        // and causes database compatibility issues
    }

    /**
     * Save multiple stores efficiently
     * 
     * @param stores List of stores to save
     */
    public void saveAll(List<Store> stores) {
        for (int i = 0; i < stores.size(); i++) {
            entityManager.persist(stores.get(i));
            if (i % 20 == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        entityManager.flush();
        entityManager.clear();
    }

    /**
     * Check if a store with given branch exists
     * 
     * @param branch The branch name to check
     * @return true if exists, false otherwise
     */
    public boolean existsByBranch(String branch) {
        Query query = entityManager.createQuery(
            "SELECT COUNT(s) FROM Store s WHERE s.branch = :branch");
        query.setParameter("branch", branch);
        Long count = (Long) query.getSingleResult();
        return count > 0;
    }

    /**
     * Get total store count for dashboard metrics
     * 
     * @return Total number of store records
     */
    public Long getTotalStoreCount() {
        Query query = entityManager.createQuery("SELECT COUNT(s) FROM Store s");
        return (Long) query.getSingleResult();
    }
}
