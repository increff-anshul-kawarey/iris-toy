package com.iris.increff.dao;

import com.iris.increff.model.NoosResult;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Date;


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

    // Get results ordered by latest calculated date (DESC)
    public List<NoosResult> getLatestResults() {
        String hql = "FROM NoosResult n ORDER BY n.calculatedDate DESC";
        TypedQuery<NoosResult> query = entityManager.createQuery(hql, NoosResult.class);
        return query.getResultList();
    }

    /**
     * Get the latest algorithm run identifier present in NOOS results.
     *
     * @return latest non-null algorithmRunId or null if no results exist
     */
    public Long getLatestRunId() {
        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT MAX(n.algorithmRunId) FROM NoosResult n", Long.class);
        return query.getSingleResult();
    }

    // Get results by specific algorithm run ID
    public List<NoosResult> getResultsByRunId(Long algorithmRunId) {
        String hql = "FROM NoosResult WHERE algorithmRunId = :runId ORDER BY styleRevContribution DESC";
        TypedQuery<NoosResult> query = entityManager.createQuery(hql, NoosResult.class);
        query.setParameter("runId", algorithmRunId);
        return query.getResultList();
    }

    /**
     * Get distinct recent run identifiers ordered descending by run id.
     *
     * @param limit maximum number of run identifiers to return
     * @return list of recent algorithmRunId values
     */
    public List<Long> getRecentRunIds(int limit) {
        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT DISTINCT n.algorithmRunId FROM NoosResult n ORDER BY n.algorithmRunId DESC",
                Long.class
        );
        query.setMaxResults(limit);
        return query.getResultList();
    }

    /**
     * Get the latest calculated date within a given run (for display).
     *
     * @param runId algorithmRunId value
     * @return latest calculatedDate within the run, or null if none
     */
    public Date getRunDate(Long runId) {
        TypedQuery<Date> query = entityManager.createQuery(
                "SELECT MAX(n.calculatedDate) FROM NoosResult n WHERE n.algorithmRunId = :runId",
                Date.class
        );
        query.setParameter("runId", runId);
        return query.getSingleResult();
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

    /**
     * Get results by type for a specific algorithm run.
     *
     * @param type result classification type (core, bestseller, fashion)
     * @param runId algorithmRunId to filter by
     * @return ordered results for the type within the run
     */
    public List<NoosResult> getResultsByTypeAndRunId(String type, Long runId) {
        String hql = "FROM NoosResult WHERE type = :type AND algorithmRunId = :runId ORDER BY styleRevContribution DESC";
        TypedQuery<NoosResult> query = entityManager.createQuery(hql, NoosResult.class);
        query.setParameter("type", type);
        query.setParameter("runId", runId);
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

    // Get count by type for dashboard display (all runs)
    public long getCountByType(String type) {
        String hql = "SELECT COUNT(*) FROM NoosResult WHERE type = :type";
        TypedQuery<Long> query = entityManager.createQuery(hql, Long.class);
        query.setParameter("type", type);
        return query.getSingleResult();
    }

    /**
     * Get result count by type limited to a specific algorithm run.
     *
     * @param type result classification type (core, bestseller, fashion)
     * @param runId algorithmRunId to filter by
     * @return count of results of the given type within the run
     */
    public long getCountByTypeForRun(String type, Long runId) {
        String hql = "SELECT COUNT(*) FROM NoosResult WHERE type = :type AND algorithmRunId = :runId";
        TypedQuery<Long> query = entityManager.createQuery(hql, Long.class);
        query.setParameter("type", type);
        query.setParameter("runId", runId);
        return query.getSingleResult();
    }

    // Get a specific result by ID
    public NoosResult select(Long id) {
        return entityManager.find(NoosResult.class, id);
    }
}
