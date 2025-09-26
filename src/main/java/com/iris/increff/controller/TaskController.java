package com.iris.increff.controller;

import com.iris.increff.dao.TaskDao;
import com.iris.increff.model.Task;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Task Management Controller
 * 
 * Provides endpoints for monitoring and managing asynchronous tasks:
 * - Get task status and progress
 * - Cancel running tasks
 * - List recent tasks for audit
 * 
 * PRD Requirement: "Maintaining Task and Audit Tables"
 * 
 * @author Anshuk Kawarry
 * @version 1.0
 * @since 2025-01-01
 */
@Api
@RestController
public class TaskController {

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);

    @Autowired
    private TaskDao taskDao;

    /**
     * Get task status and progress
     * 
     * Used by UI to poll for task completion and show progress bars
     * 
     * @param taskId Task ID to check
     * @return Task with current status and progress
     */
    @ApiOperation(value = "Get task status and progress")
    @RequestMapping(path = "/api/tasks/{taskId}", method = RequestMethod.GET)
    public ResponseEntity<Task> getTaskStatus(@PathVariable Long taskId) {
        logger.debug("📊 Getting status for task: {}", taskId);
        
        try {
            Task task = taskDao.select(taskId);
            
            if (task == null) {
                logger.warn("⚠️ Task not found: {}", taskId);
                return ResponseEntity.notFound().build();
            }
            
            logger.debug("✅ Task {} status: {} ({}%)", 
                        taskId, task.getStatus(), task.getProgressPercentage());
            
            return ResponseEntity.ok(task);
            
        } catch (Exception e) {
            logger.error("❌ Failed to get task status for {}: {}", taskId, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Request task cancellation
     * 
     * Sets cancellation flag - the actual task must check this flag
     * and stop gracefully. Not all tasks support cancellation.
     * 
     * @param taskId Task ID to cancel
     * @return 202 if cancellation requested, 404 if task not found
     */
    @ApiOperation(value = "Request task cancellation")
    @RequestMapping(path = "/api/tasks/{taskId}/cancel", method = RequestMethod.POST)
    public ResponseEntity<Void> cancelTask(@PathVariable Long taskId) {
        logger.info("🛑 Cancellation requested for task: {}", taskId);
        
        try {
            Task task = taskDao.select(taskId);
            
            if (task == null) {
                logger.warn("⚠️ Cannot cancel - task not found: {}", taskId);
                return ResponseEntity.notFound().build();
            }
            
            if (task.isCompleted() || task.isFailed() || task.isCancelled()) {
                logger.warn("⚠️ Cannot cancel - task already finished: {} (status: {})", 
                           taskId, task.getStatus());
                return ResponseEntity.badRequest().build();
            }
            
            // Set cancellation flag
            task.requestCancellation();
            task.updateProgress(task.getProgressPercentage(), "CANCELLING", "Cancellation requested");
            taskDao.update(task);
            
            logger.info("✅ Cancellation requested for task: {}", taskId);
            return ResponseEntity.accepted().build(); // HTTP 202
            
        } catch (Exception e) {
            logger.error("❌ Failed to cancel task {}: {}", taskId, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Get recent tasks for audit and monitoring
     * 
     * Returns last 50 tasks ordered by creation date (newest first)
     * Useful for admin dashboard and debugging
     * 
     * @param limit Optional limit (default 50, max 200)
     * @return List of recent tasks
     */
    @ApiOperation(value = "Get recent tasks for audit")
    @RequestMapping(path = "/api/tasks", method = RequestMethod.GET)
    public ResponseEntity<List<Task>> getRecentTasks(@RequestParam(defaultValue = "50") int limit) {
        logger.debug("📋 Getting recent tasks (limit: {})", limit);
        
        try {
            // Enforce reasonable limits
            if (limit > 200) {
                limit = 200;
                logger.warn("⚠️ Limit capped at 200");
            }
            
            List<Task> tasks = taskDao.findRecentTasks(limit);
            
            logger.debug("✅ Retrieved {} recent tasks", tasks.size());
            return ResponseEntity.ok(tasks);
            
        } catch (Exception e) {
            logger.error("❌ Failed to get recent tasks: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Get tasks by status
     * 
     * Useful for monitoring running tasks or investigating failures
     * 
     * @param status Task status to filter by
     * @param limit Optional limit (default 20, max 100)
     * @return List of tasks with specified status
     */
    @ApiOperation(value = "Get tasks by status")
    @RequestMapping(path = "/api/tasks/status/{status}", method = RequestMethod.GET)
    public ResponseEntity<List<Task>> getTasksByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "20") int limit) {
        
        logger.debug("📋 Getting tasks with status: {} (limit: {})", status, limit);
        
        try {
            // Validate status
            if (!isValidStatus(status)) {
                logger.warn("⚠️ Invalid status: {}", status);
                return ResponseEntity.badRequest().build();
            }
            
            // Enforce reasonable limits
            if (limit > 100) {
                limit = 100;
                logger.warn("⚠️ Limit capped at 100");
            }
            
            List<Task> tasks = taskDao.findByStatus(status.toUpperCase(), limit);
            
            logger.debug("✅ Retrieved {} tasks with status: {}", tasks.size(), status);
            return ResponseEntity.ok(tasks);
            
        } catch (Exception e) {
            logger.error("❌ Failed to get tasks by status {}: {}", status, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Get task statistics for dashboard
     * 
     * Returns counts by status for monitoring dashboard
     * 
     * @return Task statistics
     */
    @ApiOperation(value = "Get task statistics")
    @RequestMapping(path = "/api/tasks/stats", method = RequestMethod.GET)
    public ResponseEntity<TaskStats> getTaskStats() {
        logger.debug("📊 Getting task statistics");
        
        try {
            TaskStats stats = new TaskStats();
            stats.setRunning(taskDao.countByStatus("RUNNING") + taskDao.countByStatus("PENDING"));
            stats.setCompleted(taskDao.countByStatus("COMPLETED"));
            stats.setFailed(taskDao.countByStatus("FAILED"));
            stats.setCancelled(taskDao.countByStatus("CANCELLED"));
            stats.setTotal(stats.getRunning() + stats.getCompleted() + stats.getFailed() + stats.getCancelled());
            
            logger.debug("✅ Task stats: {} total, {} running, {} completed, {} failed, {} cancelled",
                        stats.getTotal(), stats.getRunning(), stats.getCompleted(), 
                        stats.getFailed(), stats.getCancelled());
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            logger.error("❌ Failed to get task statistics: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Validate task status
     */
    private boolean isValidStatus(String status) {
        if (status == null) return false;
        String upperStatus = status.toUpperCase();
        return "PENDING".equals(upperStatus) || 
               "RUNNING".equals(upperStatus) || 
               "COMPLETED".equals(upperStatus) || 
               "FAILED".equals(upperStatus) || 
               "CANCELLED".equals(upperStatus);
    }

    /**
     * Task statistics DTO
     */
    public static class TaskStats {
        private long total;
        private long running;
        private long completed;
        private long failed;
        private long cancelled;

        // Getters and setters
        public long getTotal() { return total; }
        public void setTotal(long total) { this.total = total; }
        
        public long getRunning() { return running; }
        public void setRunning(long running) { this.running = running; }
        
        public long getCompleted() { return completed; }
        public void setCompleted(long completed) { this.completed = completed; }
        
        public long getFailed() { return failed; }
        public void setFailed(long failed) { this.failed = failed; }
        
        public long getCancelled() { return cancelled; }
        public void setCancelled(long cancelled) { this.cancelled = cancelled; }
    }
}
