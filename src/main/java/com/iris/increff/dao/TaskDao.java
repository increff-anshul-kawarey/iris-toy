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
     * Delete all tasks (for test cleanup)
     */
    public void deleteAll() {
        entityManager.createQuery("DELETE FROM Task").executeUpdate();
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

    /**
     * Get recent task statistics for dashboard
     * @param days Number of days to look back
     * @return Array with [totalTasks, completedTasks]
     */
    public long[] getRecentTaskStats(int days) {
        // Calculate cutoff date
        long cutoffTime = System.currentTimeMillis() - (days * 24L * 60L * 60L * 1000L);
        java.util.Date cutoffDate = new java.util.Date(cutoffTime);
        
        // Total tasks in last X days
        String totalHql = "SELECT COUNT(t) FROM Task t WHERE t.createdDate >= :cutoffDate";
        TypedQuery<Long> totalQuery = entityManager.createQuery(totalHql, Long.class);
        totalQuery.setParameter("cutoffDate", cutoffDate);
        long totalTasks = totalQuery.getSingleResult();
        
        // Completed tasks in last X days
        String completedHql = "SELECT COUNT(t) FROM Task t WHERE t.createdDate >= :cutoffDate AND t.status = 'COMPLETED'";
        TypedQuery<Long> completedQuery = entityManager.createQuery(completedHql, Long.class);
        completedQuery.setParameter("cutoffDate", cutoffDate);
        long completedTasks = completedQuery.getSingleResult();
        
        return new long[]{totalTasks, completedTasks};
    }

    /**
     * Get count of active tasks (PENDING + RUNNING)
     */
    public int getActiveTaskCount() {
        String hql = "SELECT COUNT(t) FROM Task t WHERE t.status IN ('PENDING', 'RUNNING')";
        TypedQuery<Long> query = entityManager.createQuery(hql, Long.class);
        return query.getSingleResult().intValue();
    }

    /**
     * Get recent task statistics filtered by taskType
     * @param taskType Task type value (e.g., STYLES_UPLOAD, SALES_UPLOAD, ALGORITHM_RUN)
     * @param days Number of days to look back
     * @return Array with [totalTasks, completedTasks, failedTasks]
     */
    public long[] getRecentTaskStatsByType(String taskType, int days) {
        long cutoffTime = System.currentTimeMillis() - (days * 24L * 60L * 60L * 1000L);
        java.util.Date cutoffDate = new java.util.Date(cutoffTime);

        String totalHql = "SELECT COUNT(t) FROM Task t WHERE t.createdDate >= :cutoffDate AND t.taskType = :taskType";
        TypedQuery<Long> totalQuery = entityManager.createQuery(totalHql, Long.class);
        totalQuery.setParameter("cutoffDate", cutoffDate);
        totalQuery.setParameter("taskType", taskType);
        long totalTasks = totalQuery.getSingleResult();

        String completedHql = "SELECT COUNT(t) FROM Task t WHERE t.createdDate >= :cutoffDate AND t.taskType = :taskType AND t.status = 'COMPLETED'";
        TypedQuery<Long> completedQuery = entityManager.createQuery(completedHql, Long.class);
        completedQuery.setParameter("cutoffDate", cutoffDate);
        completedQuery.setParameter("taskType", taskType);
        long completedTasks = completedQuery.getSingleResult();

        String failedHql = "SELECT COUNT(t) FROM Task t WHERE t.createdDate >= :cutoffDate AND t.taskType = :taskType AND t.status = 'FAILED'";
        TypedQuery<Long> failedQuery = entityManager.createQuery(failedHql, Long.class);
        failedQuery.setParameter("cutoffDate", cutoffDate);
        failedQuery.setParameter("taskType", taskType);
        long failedTasks = failedQuery.getSingleResult();

        return new long[]{totalTasks, completedTasks, failedTasks};
    }

    /**
     * Get tasks by type in last N days
     */
    public List<Task> getTasksByTypeSince(String taskType, int days) {
        long cutoffTime = System.currentTimeMillis() - (days * 24L * 60L * 60L * 1000L);
        java.util.Date cutoffDate = new java.util.Date(cutoffTime);

        String hql = "FROM Task t WHERE t.createdDate >= :cutoffDate AND t.taskType = :taskType ORDER BY t.startTime DESC";
        TypedQuery<Task> query = entityManager.createQuery(hql, Task.class);
        query.setParameter("cutoffDate", cutoffDate);
        query.setParameter("taskType", taskType);
        return query.getResultList();
    }
}
