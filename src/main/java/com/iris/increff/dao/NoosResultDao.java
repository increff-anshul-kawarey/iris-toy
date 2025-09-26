package com.iris.increff.dao;

import com.iris.increff.model.NoosResult;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;


@Repository
public class NoosResultDao {

    @PersistenceContext
    private EntityManager entityManager;

    // Insert a single NOOS result
    public void insert(NoosResult noosResult) {
        entityManager.persist(noosResult);
    }

    // Insert multiple NOOS results efficiently
    // Uses batch processing for better performance with large datasets
    public void insertAll(List<NoosResult> results) {
        int batchSize = 50; // Process in batches of 50
        
        for (int i = 0; i < results.size(); i++) {
            entityManager.persist(results.get(i));
            
            // Flush and clear session every batch to avoid memory issues
            if (i % batchSize == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        
        // Final flush for remaining items
        entityManager.flush();
        entityManager.clear();
    }

    // Get the most recent NOOS results
    public List<NoosResult> getLatestResults() {
        String hql = "FROM NoosResult ORDER BY calculatedDate DESC";
        TypedQuery<NoosResult> query = entityManager.createQuery(hql, NoosResult.class);
        query.setMaxResults(1000); // Limit to latest 1000 results
        return query.getResultList();
    }

    // Get results by specific algorithm run ID
    public List<NoosResult> getResultsByRunId(Long algorithmRunId) {
        String hql = "FROM NoosResult WHERE algorithmRunId = :runId ORDER BY styleCode";
        TypedQuery<NoosResult> query = entityManager.createQuery(hql, NoosResult.class);
        query.setParameter("runId", algorithmRunId);
        return query.getResultList();
    }

    // Get results by category
    public List<NoosResult> getResultsByCategory(String category) {
        String hql = "FROM NoosResult WHERE category = :category ORDER BY styleRevContribution DESC";
        TypedQuery<NoosResult> query = entityManager.createQuery(hql, NoosResult.class);
        query.setParameter("category", category);
        return query.getResultList();
    }

    // Get results by type (core, bestseller, fashion)
    public List<NoosResult> getResultsByType(String type) {
        String hql = "FROM NoosResult WHERE type = :type ORDER BY styleRevContribution DESC";
        TypedQuery<NoosResult> query = entityManager.createQuery(hql, NoosResult.class);
        query.setParameter("type", type);
        return query.getResultList();
    }

    // Delete all NOOS results
    public void deleteAll() {
        entityManager.createQuery("DELETE FROM NoosResult").executeUpdate();
    }

    // Get total count of NOOS results
    public long getCount() {
        String hql = "SELECT COUNT(*) FROM NoosResult";
        return entityManager.createQuery(hql, Long.class).getSingleResult();
    }

    // Get count by type for dashboard display
    public long getCountByType(String type) {
        String hql = "SELECT COUNT(*) FROM NoosResult WHERE type = :type";
        TypedQuery<Long> query = entityManager.createQuery(hql, Long.class);
        query.setParameter("type", type);
        return query.getSingleResult();
    }

    // Get a specific result by ID
    public NoosResult select(Long id) {
        return entityManager.find(NoosResult.class, id);
    }
}
