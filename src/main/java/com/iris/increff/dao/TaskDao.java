package com.iris.increff.dao;

import com.iris.increff.model.Task;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;


@Repository
public class TaskDao {

    @PersistenceContext
    private EntityManager entityManager;

    // Insert a new task
    public void insert(Task task) {
        entityManager.persist(task);
    }

    // Update an existing task
    public void update(Task task) {
        entityManager.merge(task);
    }

    // Get a task by ID
    public Task select(Long id) {
        return entityManager.find(Task.class, id);
    }

    // Get recent tasks for dashboard/monitoring
    public List<Task> getRecentTasks(int limit) {
        String hql = "FROM Task ORDER BY startTime DESC";
        TypedQuery<Task> query = entityManager.createQuery(hql, Task.class);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    // Get all currently running tasks
    public List<Task> getRunningTasks() {
        String hql = "FROM Task WHERE status IN ('PENDING', 'RUNNING') ORDER BY startTime";
        TypedQuery<Task> query = entityManager.createQuery(hql, Task.class);
        return query.getResultList();
    }

    // Get tasks by type for analysis
    public List<Task> getTasksByType(String taskType) {
        String hql = "FROM Task WHERE taskType = :taskType ORDER BY startTime DESC";
        TypedQuery<Task> query = entityManager.createQuery(hql, Task.class);
        query.setParameter("taskType", taskType);
        return query.getResultList();
    }

    // Get tasks by status
    public List<Task> getTasksByStatus(String status) {
        String hql = "FROM Task WHERE status = :status ORDER BY startTime DESC";
        TypedQuery<Task> query = entityManager.createQuery(hql, Task.class);
        query.setParameter("status", status);
        return query.getResultList();
    }

    // Get failed tasks for troubleshooting
    public List<Task> getFailedTasks() {
        String hql = "FROM Task WHERE status = 'FAILED' ORDER BY startTime DESC";
        TypedQuery<Task> query = entityManager.createQuery(hql, Task.class);
        return query.getResultList();
    }

    // Get total count of tasks
    public long getCount() {
        String hql = "SELECT COUNT(*) FROM Task";
        return entityManager.createQuery(hql, Long.class).getSingleResult();
    }

    // Get count of tasks by status for dashboard
    public long getCountByStatus(String status) {
        String hql = "SELECT COUNT(*) FROM Task WHERE status = :status";
        TypedQuery<Long> query = entityManager.createQuery(hql, Long.class);
        query.setParameter("status", status);
        return query.getSingleResult();
    }

    // Delete old completed tasks (for cleanup)
    public void deleteOldCompletedTasks(int daysToKeep) {
        String hql = "DELETE FROM Task WHERE status = 'COMPLETED' " +
                    "AND endTime < :cutoffDate";
        
        // Calculate cutoff date
        long cutoffTime = System.currentTimeMillis() - (daysToKeep * 24L * 60L * 60L * 1000L);
        java.util.Date cutoffDate = new java.util.Date(cutoffTime);
        
        entityManager.createQuery(hql)
                   .setParameter("cutoffDate", cutoffDate)
                   .executeUpdate();
    }

    // Get all tasks (for admin/debugging purposes)
    public List<Task> selectAll() {
        String hql = "FROM Task ORDER BY startTime DESC";
        TypedQuery<Task> query = entityManager.createQuery(hql, Task.class);
        return query.getResultList();
    }

    /**
     * Get recent tasks with limit
     */
    public List<Task> findRecentTasks(int limit) {
        String hql = "FROM Task ORDER BY createdDate DESC";
        TypedQuery<Task> query = entityManager.createQuery(hql, Task.class);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    /**
     * Find tasks by status with limit
     */
    public List<Task> findByStatus(String status, int limit) {
        String hql = "FROM Task WHERE status = :status ORDER BY createdDate DESC";
        TypedQuery<Task> query = entityManager.createQuery(hql, Task.class);
        query.setParameter("status", status);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    /**
     * Count tasks by status
     */
    public long countByStatus(String status) {
        String hql = "SELECT COUNT(t) FROM Task t WHERE t.status = :status";
        TypedQuery<Long> query = entityManager.createQuery(hql, Long.class);
        query.setParameter("status", status);
        return query.getSingleResult();
    }
}
