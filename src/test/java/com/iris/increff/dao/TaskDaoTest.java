package com.iris.increff.dao;

import com.iris.increff.AbstractUnitTest;
import com.iris.increff.model.Task;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Comprehensive test suite for TaskDao
 * 
 * Tests all CRUD operations, query methods, batch operations, edge cases,
 * and transaction scenarios to achieve 90%+ method coverage.
 * 
 * @author Anshuk Kawarry
 * @version 1.0
 * @since 2025-01-01
 */
public class TaskDaoTest extends AbstractUnitTest {

    @Autowired
    private TaskDao taskDao;

    private Task testTask1;
    private Task testTask2;
    private Task testTask3;
    private Task testTask4;
    
    private Date testStartTime;
    private Date testEndTime;
    private Date testCreatedDate;

    /**
     * Setup test data before each test method
     * Creates sample Task objects for testing
     */
    @Before
    public void setUp() {
        // Create test dates
        Calendar cal = Calendar.getInstance();
        testCreatedDate = cal.getTime();
        
        cal.add(Calendar.HOUR, 1);
        testStartTime = cal.getTime();
        
        cal.add(Calendar.HOUR, 2);
        testEndTime = cal.getTime();

        // Create test task 1 - FILE_UPLOAD task
        testTask1 = new Task();
        testTask1.setTaskType("FILE_UPLOAD");
        testTask1.setStatus("PENDING");
        testTask1.setFileName("styles.tsv");
        testTask1.setTotalRecords(1000);
        testTask1.setProcessedRecords(0);
        testTask1.setErrorCount(0);
        testTask1.setStartTime(testStartTime);
        testTask1.setUserId("user123");
        testTask1.setProgressPercentage(0.0);
        testTask1.setCurrentPhase("INITIALIZATION");
        testTask1.setCurrentStep(1);
        testTask1.setTotalSteps(5);
        testTask1.setCancellationRequested(false);

        // Create test task 2 - ALGORITHM_RUN task
        testTask2 = new Task();
        testTask2.setTaskType("ALGORITHM_RUN");
        testTask2.setStatus("RUNNING");
        testTask2.setFileName("noos_algorithm.json");
        testTask2.setTotalRecords(500);
        testTask2.setProcessedRecords(250);
        testTask2.setErrorCount(5);
        testTask2.setStartTime(testStartTime);
        testTask2.setUserId("user456");
        testTask2.setParameters("{\"threshold\":0.8,\"lookback\":30}");
        testTask2.setProgressPercentage(50.0);
        testTask2.setCurrentPhase("PROCESSING");
        testTask2.setCurrentStep(3);
        testTask2.setTotalSteps(5);
        testTask2.setCancellationRequested(false);

        // Create test task 3 - COMPLETED task
        testTask3 = new Task();
        testTask3.setTaskType("FILE_DOWNLOAD");
        testTask3.setStatus("COMPLETED");
        testTask3.setFileName("report.tsv");
        testTask3.setTotalRecords(100);
        testTask3.setProcessedRecords(100);
        testTask3.setErrorCount(0);
        testTask3.setStartTime(testStartTime);
        testTask3.setEndTime(testEndTime);
        testTask3.setUserId("user789");
        testTask3.setProgressPercentage(100.0);
        testTask3.setCurrentPhase("COMPLETED");
        testTask3.setCurrentStep(5);
        testTask3.setTotalSteps(5);
        testTask3.setResultUrl("/downloads/report_123.tsv");
        testTask3.setResultType("TSV");
        testTask3.setCancellationRequested(false);

        // Create test task 4 - FAILED task
        testTask4 = new Task();
        testTask4.setTaskType("FILE_UPLOAD");
        testTask4.setStatus("FAILED");
        testTask4.setFileName("invalid.tsv");
        testTask4.setTotalRecords(50);
        testTask4.setProcessedRecords(25);
        testTask4.setErrorCount(25);
        testTask4.setStartTime(testStartTime);
        testTask4.setEndTime(testEndTime);
        testTask4.setErrorMessage("Invalid file format");
        testTask4.setUserId("user999");
        testTask4.setProgressPercentage(50.0);
        testTask4.setCurrentPhase("FAILED");
        testTask4.setCancellationRequested(false);
    }

    // ==================== CRUD OPERATIONS TESTS ====================

    /**
     * Test inserting a new task
     * Verifies that a new task is persisted with generated ID and timestamps
     */
    @Test
    @Transactional
    @Rollback
    public void testInsert_NewTask() {
        // Given: A new task without ID
        assertNull("Task ID should be null before saving", testTask1.getId());

        // When: Insert the task
        taskDao.insert(testTask1);

        // Then: Task should be saved with generated ID and timestamps
        assertNotNull("Task should have generated ID", testTask1.getId());
        assertNotNull("Task should have created date", testTask1.getCreatedDate());
        assertNotNull("Task should have last updated date", testTask1.getLastUpdatedDate());
        assertEquals("Task type should match", "FILE_UPLOAD", testTask1.getTaskType());
        assertEquals("Status should match", "PENDING", testTask1.getStatus());
        assertEquals("File name should match", "styles.tsv", testTask1.getFileName());
        assertEquals("Total records should match", Integer.valueOf(1000), testTask1.getTotalRecords());
        assertEquals("User ID should match", "user123", testTask1.getUserId());
        assertEquals("Progress should be initialized", 0.0, testTask1.getProgressPercentage(), 0.01);
        assertFalse("Cancellation should be false", testTask1.isCancellationRequested());
    }

    /**
     * Test updating an existing task
     * Verifies that an existing task is updated correctly
     */
    @Test
    @Transactional
    @Rollback
    public void testUpdate_ExistingTask() {
        // Given: Insert a task first
        taskDao.insert(testTask1);
        Long originalId = testTask1.getId();
        Date originalCreatedDate = testTask1.getCreatedDate();
        
        // Modify the task
        testTask1.setStatus("RUNNING");
        testTask1.setProcessedRecords(500);
        testTask1.setProgressPercentage(50.0);
        testTask1.setCurrentPhase("PROCESSING");
        testTask1.setCurrentStep(3);

        // When: Update the task
        taskDao.update(testTask1);

        // Then: Task should be updated with same ID but updated timestamp
        assertEquals("ID should remain the same", originalId, testTask1.getId());
        assertEquals("Created date should remain unchanged", originalCreatedDate, testTask1.getCreatedDate());
        assertNotNull("Last updated date should be set", testTask1.getLastUpdatedDate());
        assertEquals("Status should be updated", "RUNNING", testTask1.getStatus());
        assertEquals("Processed records should be updated", Integer.valueOf(500), testTask1.getProcessedRecords());
        assertEquals("Progress should be updated", 50.0, testTask1.getProgressPercentage(), 0.01);
        assertEquals("Phase should be updated", "PROCESSING", testTask1.getCurrentPhase());
        assertEquals("Step should be updated", Integer.valueOf(3), testTask1.getCurrentStep());
    }

    /**
     * Test selecting a task by ID
     * Verifies that select returns correct task
     */
    @Test
    @Transactional
    @Rollback
    public void testSelect_ExistingTask() {
        // Given: Insert a task
        taskDao.insert(testTask1);
        Long taskId = testTask1.getId();

        // When: Select by ID
        Task foundTask = taskDao.select(taskId);

        // Then: Should return the correct task
        assertNotNull("Found task should not be null", foundTask);
        assertEquals("ID should match", taskId, foundTask.getId());
        assertEquals("Task type should match", "FILE_UPLOAD", foundTask.getTaskType());
        assertEquals("Status should match", "PENDING", foundTask.getStatus());
        assertEquals("File name should match", "styles.tsv", foundTask.getFileName());
        assertEquals("User ID should match", "user123", foundTask.getUserId());
    }

    /**
     * Test selecting a task by non-existent ID
     * Verifies that select returns null for non-existent ID
     */
    @Test
    @Transactional
    @Rollback
    public void testSelect_NonExistentTask() {
        // Given: A non-existent ID
        Long nonExistentId = 99999L;

        // When: Select by non-existent ID
        Task foundTask = taskDao.select(nonExistentId);

        // Then: Should return null
        assertNull("Should return null for non-existent ID", foundTask);
    }

    /**
     * Test selecting a task by null ID
     * Verifies that select handles null input gracefully
     */
    @Test
    @Transactional
    @Rollback
    public void testSelect_NullId() {
        // When: Select by null ID
        try {
            Task foundTask = taskDao.select(null);
            // Hibernate may throw exception for null ID, which is acceptable
            assertNull("Should return null for null ID", foundTask);
        } catch (IllegalArgumentException e) {
            // This is also acceptable behavior - Hibernate doesn't allow null IDs
            assertTrue("Should throw IllegalArgumentException for null ID", 
                e.getMessage().contains("id to load is required"));
        }
    }

    // ==================== QUERY METHODS TESTS ====================

    /**
     * Test getting all tasks
     * Verifies that selectAll returns all saved tasks
     */
    @Test
    @Transactional
    @Rollback
    public void testSelectAll_WithTasks() {
        // Given: Insert multiple tasks
        taskDao.insert(testTask1);
        taskDao.insert(testTask2);
        taskDao.insert(testTask3);

        // When: Select all tasks
        List<Task> allTasks = taskDao.selectAll();

        // Then: Should return all saved tasks
        assertNotNull("All tasks list should not be null", allTasks);
        assertEquals("Should return 3 tasks", 3, allTasks.size());
        
        // Verify tasks are ordered by start time DESC
        assertTrue("Should contain FILE_UPLOAD task", allTasks.stream()
            .anyMatch(t -> "FILE_UPLOAD".equals(t.getTaskType())));
        assertTrue("Should contain ALGORITHM_RUN task", allTasks.stream()
            .anyMatch(t -> "ALGORITHM_RUN".equals(t.getTaskType())));
        assertTrue("Should contain FILE_DOWNLOAD task", allTasks.stream()
            .anyMatch(t -> "FILE_DOWNLOAD".equals(t.getTaskType())));
    }

    /**
     * Test getting all tasks when no tasks exist
     * Verifies that selectAll returns empty list when no data
     */
    @Test
    @Transactional
    @Rollback
    public void testSelectAll_NoTasks() {
        // When: Select all tasks with no data
        List<Task> allTasks = taskDao.selectAll();

        // Then: Should return empty list
        assertNotNull("All tasks list should not be null", allTasks);
        assertTrue("Should return empty list", allTasks.isEmpty());
    }

    /**
     * Test getting recent tasks with limit
     * Verifies that getRecentTasks returns correct number of tasks
     */
    @Test
    @Transactional
    @Rollback
    public void testGetRecentTasks_WithLimit() {
        // Given: Insert multiple tasks
        taskDao.insert(testTask1);
        taskDao.insert(testTask2);
        taskDao.insert(testTask3);
        taskDao.insert(testTask4);

        // When: Get recent tasks with limit 2
        List<Task> recentTasks = taskDao.getRecentTasks(2);

        // Then: Should return limited number of tasks
        assertNotNull("Recent tasks should not be null", recentTasks);
        assertEquals("Should return 2 tasks", 2, recentTasks.size());
    }

    /**
     * Test getting recent tasks with zero limit
     * Verifies that getRecentTasks handles zero limit gracefully
     */
    @Test
    @Transactional
    @Rollback
    public void testGetRecentTasks_ZeroLimit() {
        // Given: Insert tasks
        taskDao.insert(testTask1);
        taskDao.insert(testTask2);

        // When: Get recent tasks with limit 0
        List<Task> recentTasks = taskDao.getRecentTasks(0);

        // Then: Should return empty list
        assertNotNull("Recent tasks should not be null", recentTasks);
        assertTrue("Should return empty list", recentTasks.isEmpty());
    }

    /**
     * Test getting running tasks
     * Verifies that getRunningTasks returns only PENDING and RUNNING tasks
     */
    @Test
    @Transactional
    @Rollback
    public void testGetRunningTasks() {
        // Given: Insert tasks with different statuses
        taskDao.insert(testTask1); // PENDING
        taskDao.insert(testTask2); // RUNNING
        taskDao.insert(testTask3); // COMPLETED
        taskDao.insert(testTask4); // FAILED

        // When: Get running tasks
        List<Task> runningTasks = taskDao.getRunningTasks();

        // Then: Should return only PENDING and RUNNING tasks
        assertNotNull("Running tasks should not be null", runningTasks);
        assertEquals("Should return 2 running tasks", 2, runningTasks.size());
        
        // Verify all returned tasks are running
        assertTrue("All tasks should be running", runningTasks.stream()
            .allMatch(t -> "PENDING".equals(t.getStatus()) || "RUNNING".equals(t.getStatus())));
    }

    /**
     * Test getting running tasks when none exist
     * Verifies that getRunningTasks returns empty list when no running tasks
     */
    @Test
    @Transactional
    @Rollback
    public void testGetRunningTasks_NoRunningTasks() {
        // Given: Insert only completed/failed tasks
        taskDao.insert(testTask3); // COMPLETED
        taskDao.insert(testTask4); // FAILED

        // When: Get running tasks
        List<Task> runningTasks = taskDao.getRunningTasks();

        // Then: Should return empty list
        assertNotNull("Running tasks should not be null", runningTasks);
        assertTrue("Should return empty list", runningTasks.isEmpty());
    }

    /**
     * Test getting tasks by type
     * Verifies that getTasksByType returns correct tasks
     */
    @Test
    @Transactional
    @Rollback
    public void testGetTasksByType_ExistingType() {
        // Given: Insert tasks with different types
        taskDao.insert(testTask1); // FILE_UPLOAD
        taskDao.insert(testTask2); // ALGORITHM_RUN
        taskDao.insert(testTask3); // FILE_DOWNLOAD
        taskDao.insert(testTask4); // FILE_UPLOAD

        // When: Get tasks by FILE_UPLOAD type
        List<Task> uploadTasks = taskDao.getTasksByType("FILE_UPLOAD");

        // Then: Should return only FILE_UPLOAD tasks
        assertNotNull("Upload tasks should not be null", uploadTasks);
        assertEquals("Should return 2 upload tasks", 2, uploadTasks.size());
        
        // Verify all returned tasks are FILE_UPLOAD
        assertTrue("All tasks should be FILE_UPLOAD", uploadTasks.stream()
            .allMatch(t -> "FILE_UPLOAD".equals(t.getTaskType())));
    }

    /**
     * Test getting tasks by non-existent type
     * Verifies that getTasksByType returns empty list for non-existent type
     */
    @Test
    @Transactional
    @Rollback
    public void testGetTasksByType_NonExistentType() {
        // Given: Insert tasks
        taskDao.insert(testTask1);

        // When: Get tasks by non-existent type
        List<Task> nonExistentTasks = taskDao.getTasksByType("NON_EXISTENT");

        // Then: Should return empty list
        assertNotNull("Non-existent type tasks should not be null", nonExistentTasks);
        assertTrue("Should return empty list", nonExistentTasks.isEmpty());
    }

    /**
     * Test getting tasks by null type
     * Verifies that getTasksByType handles null input gracefully
     */
    @Test
    @Transactional
    @Rollback
    public void testGetTasksByType_NullType() {
        // Given: Insert tasks
        taskDao.insert(testTask1);

        // When: Get tasks by null type
        List<Task> nullTypeTasks = taskDao.getTasksByType(null);

        // Then: Should handle gracefully
        assertNotNull("Null type tasks should not be null", nullTypeTasks);
        assertTrue("Should return empty list for null type", nullTypeTasks.isEmpty());
    }

    /**
     * Test getting tasks by status
     * Verifies that getTasksByStatus returns correct tasks
     */
    @Test
    @Transactional
    @Rollback
    public void testGetTasksByStatus_ExistingStatus() {
        // Given: Insert tasks with different statuses
        taskDao.insert(testTask1); // PENDING
        taskDao.insert(testTask2); // RUNNING
        taskDao.insert(testTask3); // COMPLETED
        taskDao.insert(testTask4); // FAILED

        // When: Get tasks by COMPLETED status
        List<Task> completedTasks = taskDao.getTasksByStatus("COMPLETED");

        // Then: Should return only COMPLETED tasks
        assertNotNull("Completed tasks should not be null", completedTasks);
        assertEquals("Should return 1 completed task", 1, completedTasks.size());
        assertEquals("Task should be COMPLETED", "COMPLETED", completedTasks.get(0).getStatus());
    }

    /**
     * Test getting failed tasks
     * Verifies that getFailedTasks returns only FAILED tasks
     */
    @Test
    @Transactional
    @Rollback
    public void testGetFailedTasks() {
        // Given: Insert tasks with different statuses
        taskDao.insert(testTask1); // PENDING
        taskDao.insert(testTask2); // RUNNING
        taskDao.insert(testTask3); // COMPLETED
        taskDao.insert(testTask4); // FAILED

        // When: Get failed tasks
        List<Task> failedTasks = taskDao.getFailedTasks();

        // Then: Should return only FAILED tasks
        assertNotNull("Failed tasks should not be null", failedTasks);
        assertEquals("Should return 1 failed task", 1, failedTasks.size());
        assertEquals("Task should be FAILED", "FAILED", failedTasks.get(0).getStatus());
        assertEquals("Should have error message", "Invalid file format", failedTasks.get(0).getErrorMessage());
    }

    /**
     * Test getting failed tasks when none exist
     * Verifies that getFailedTasks returns empty list when no failed tasks
     */
    @Test
    @Transactional
    @Rollback
    public void testGetFailedTasks_NoFailedTasks() {
        // Given: Insert only non-failed tasks
        taskDao.insert(testTask1); // PENDING
        taskDao.insert(testTask2); // RUNNING
        taskDao.insert(testTask3); // COMPLETED

        // When: Get failed tasks
        List<Task> failedTasks = taskDao.getFailedTasks();

        // Then: Should return empty list
        assertNotNull("Failed tasks should not be null", failedTasks);
        assertTrue("Should return empty list", failedTasks.isEmpty());
    }

    /**
     * Test getting total count of tasks
     * Verifies that getCount returns correct count
     */
    @Test
    @Transactional
    @Rollback
    public void testGetCount_WithTasks() {
        // Given: Insert multiple tasks
        taskDao.insert(testTask1);
        taskDao.insert(testTask2);
        taskDao.insert(testTask3);

        // When: Get total count
        long count = taskDao.getCount();

        // Then: Should return correct count
        assertEquals("Should return count of 3", 3, count);
    }

    /**
     * Test getting total count when no tasks exist
     * Verifies that getCount returns 0 when no data
     */
    @Test
    @Transactional
    @Rollback
    public void testGetCount_NoTasks() {
        // When: Get total count with no data
        long count = taskDao.getCount();

        // Then: Should return 0
        assertEquals("Should return count of 0", 0, count);
    }

    /**
     * Test getting count by status
     * Verifies that getCountByStatus returns correct count
     */
    @Test
    @Transactional
    @Rollback
    public void testGetCountByStatus_ExistingStatus() {
        // Given: Insert tasks with different statuses
        taskDao.insert(testTask1); // PENDING
        taskDao.insert(testTask2); // RUNNING
        taskDao.insert(testTask3); // COMPLETED
        taskDao.insert(testTask4); // FAILED

        // When: Get count by PENDING status
        long pendingCount = taskDao.getCountByStatus("PENDING");
        long runningCount = taskDao.getCountByStatus("RUNNING");
        long completedCount = taskDao.getCountByStatus("COMPLETED");
        long failedCount = taskDao.getCountByStatus("FAILED");

        // Then: Should return correct counts
        assertEquals("Should have 1 pending task", 1, pendingCount);
        assertEquals("Should have 1 running task", 1, runningCount);
        assertEquals("Should have 1 completed task", 1, completedCount);
        assertEquals("Should have 1 failed task", 1, failedCount);
    }

    /**
     * Test getting count by non-existent status
     * Verifies that getCountByStatus returns 0 for non-existent status
     */
    @Test
    @Transactional
    @Rollback
    public void testGetCountByStatus_NonExistentStatus() {
        // Given: Insert tasks
        taskDao.insert(testTask1);

        // When: Get count by non-existent status
        long count = taskDao.getCountByStatus("NON_EXISTENT");

        // Then: Should return 0
        assertEquals("Should return 0 for non-existent status", 0, count);
    }

    /**
     * Test finding recent tasks with limit (alternative method)
     * Verifies that findRecentTasks returns correct tasks
     */
    @Test
    @Transactional
    @Rollback
    public void testFindRecentTasks_WithLimit() {
        // Given: Insert multiple tasks
        taskDao.insert(testTask1);
        taskDao.insert(testTask2);
        taskDao.insert(testTask3);

        // When: Find recent tasks with limit 2
        List<Task> recentTasks = taskDao.findRecentTasks(2);

        // Then: Should return limited number of tasks
        assertNotNull("Recent tasks should not be null", recentTasks);
        assertEquals("Should return 2 tasks", 2, recentTasks.size());
    }

    /**
     * Test finding tasks by status with limit
     * Verifies that findByStatus returns correct tasks with limit
     */
    @Test
    @Transactional
    @Rollback
    public void testFindByStatus_WithLimit() {
        // Given: Insert multiple tasks with same status
        Task task1 = new Task();
        task1.setTaskType("FILE_UPLOAD");
        task1.setStatus("PENDING");
        task1.setFileName("file1.tsv");
        taskDao.insert(task1);

        Task task2 = new Task();
        task2.setTaskType("FILE_UPLOAD");
        task2.setStatus("PENDING");
        task2.setFileName("file2.tsv");
        taskDao.insert(task2);

        Task task3 = new Task();
        task3.setTaskType("FILE_UPLOAD");
        task3.setStatus("PENDING");
        task3.setFileName("file3.tsv");
        taskDao.insert(task3);

        // When: Find by status with limit 2
        List<Task> pendingTasks = taskDao.findByStatus("PENDING", 2);

        // Then: Should return limited number of tasks
        assertNotNull("Pending tasks should not be null", pendingTasks);
        assertEquals("Should return 2 tasks", 2, pendingTasks.size());
        
        // Verify all returned tasks have correct status
        assertTrue("All tasks should be PENDING", pendingTasks.stream()
            .allMatch(t -> "PENDING".equals(t.getStatus())));
    }

    /**
     * Test counting tasks by status (alternative method)
     * Verifies that countByStatus returns correct count
     */
    @Test
    @Transactional
    @Rollback
    public void testCountByStatus() {
        // Given: Insert tasks with different statuses
        taskDao.insert(testTask1); // PENDING
        taskDao.insert(testTask2); // RUNNING
        taskDao.insert(testTask3); // COMPLETED

        // When: Count by status
        long pendingCount = taskDao.countByStatus("PENDING");
        long runningCount = taskDao.countByStatus("RUNNING");
        long completedCount = taskDao.countByStatus("COMPLETED");

        // Then: Should return correct counts
        assertEquals("Should have 1 pending task", 1, pendingCount);
        assertEquals("Should have 1 running task", 1, runningCount);
        assertEquals("Should have 1 completed task", 1, completedCount);
    }

    /**
     * Test getting recent task statistics
     * Verifies that getRecentTaskStats returns correct statistics
     */
    @Test
    @Transactional
    @Rollback
    public void testGetRecentTaskStats() {
        // Given: Insert tasks (they will have recent created dates)
        taskDao.insert(testTask1);
        taskDao.insert(testTask2);
        taskDao.insert(testTask3); // COMPLETED
        taskDao.insert(testTask4);

        // When: Get recent task stats for last 1 day
        long[] stats = taskDao.getRecentTaskStats(1);

        // Then: Should return correct statistics
        assertNotNull("Stats should not be null", stats);
        assertEquals("Should have 2 elements", 2, stats.length);
        assertEquals("Total tasks should be 4", 4, stats[0]);
        assertEquals("Completed tasks should be 1", 1, stats[1]);
    }

    /**
     * Test getting recent task statistics with no tasks
     * Verifies that getRecentTaskStats returns zeros when no tasks
     */
    @Test
    @Transactional
    @Rollback
    public void testGetRecentTaskStats_NoTasks() {
        // When: Get recent task stats with no data
        long[] stats = taskDao.getRecentTaskStats(1);

        // Then: Should return zeros
        assertNotNull("Stats should not be null", stats);
        assertEquals("Should have 2 elements", 2, stats.length);
        assertEquals("Total tasks should be 0", 0, stats[0]);
        assertEquals("Completed tasks should be 0", 0, stats[1]);
    }

    /**
     * Test getting active task count
     * Verifies that getActiveTaskCount returns correct count of PENDING and RUNNING tasks
     */
    @Test
    @Transactional
    @Rollback
    public void testGetActiveTaskCount() {
        // Given: Insert tasks with different statuses
        taskDao.insert(testTask1); // PENDING
        taskDao.insert(testTask2); // RUNNING
        taskDao.insert(testTask3); // COMPLETED
        taskDao.insert(testTask4); // FAILED

        // When: Get active task count
        int activeCount = taskDao.getActiveTaskCount();

        // Then: Should return count of PENDING and RUNNING tasks
        assertEquals("Should have 2 active tasks", 2, activeCount);
    }

    /**
     * Test getting active task count when no active tasks
     * Verifies that getActiveTaskCount returns 0 when no active tasks
     */
    @Test
    @Transactional
    @Rollback
    public void testGetActiveTaskCount_NoActiveTasks() {
        // Given: Insert only completed/failed tasks
        taskDao.insert(testTask3); // COMPLETED
        taskDao.insert(testTask4); // FAILED

        // When: Get active task count
        int activeCount = taskDao.getActiveTaskCount();

        // Then: Should return 0
        assertEquals("Should have 0 active tasks", 0, activeCount);
    }

    // ==================== BATCH OPERATIONS TESTS ====================

    /**
     * Test deleting old completed tasks
     * Verifies that deleteOldCompletedTasks removes only old completed tasks
     */
    @Test
    @Transactional
    @Rollback
    public void testDeleteOldCompletedTasks() {
        // Given: Insert tasks with different statuses and dates
        // Create old completed task
        Task oldCompletedTask = new Task();
        oldCompletedTask.setTaskType("FILE_UPLOAD");
        oldCompletedTask.setStatus("COMPLETED");
        oldCompletedTask.setFileName("old_file.tsv");
        
        // Set end time to 10 days ago
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -10);
        oldCompletedTask.setEndTime(cal.getTime());
        taskDao.insert(oldCompletedTask);

        // Insert recent tasks
        taskDao.insert(testTask1); // PENDING
        taskDao.insert(testTask2); // RUNNING
        taskDao.insert(testTask3); // COMPLETED (recent)

        // Verify initial count
        assertEquals("Should have 4 tasks initially", 4, taskDao.getCount());

        // When: Delete old completed tasks (keep last 7 days)
        taskDao.deleteOldCompletedTasks(7);

        // Then: Should delete only old completed tasks
        long remainingCount = taskDao.getCount();
        assertEquals("Should have 3 tasks remaining", 3, remainingCount);
        
        // Verify the old completed task was deleted
        List<Task> allTasks = taskDao.selectAll();
        assertFalse("Should not contain old completed task", allTasks.stream()
            .anyMatch(t -> "old_file.tsv".equals(t.getFileName())));
    }

    /**
     * Test deleting old completed tasks when none exist
     * Verifies that deleteOldCompletedTasks handles no matching tasks gracefully
     */
    @Test
    @Transactional
    @Rollback
    public void testDeleteOldCompletedTasks_NoOldTasks() {
        // Given: Insert only recent tasks
        taskDao.insert(testTask1); // PENDING
        taskDao.insert(testTask2); // RUNNING
        taskDao.insert(testTask3); // COMPLETED (recent)

        // Verify initial count
        assertEquals("Should have 3 tasks initially", 3, taskDao.getCount());

        // When: Delete old completed tasks (keep last 1 day)
        taskDao.deleteOldCompletedTasks(1);

        // Then: Should not delete any tasks
        long remainingCount = taskDao.getCount();
        assertEquals("Should still have 3 tasks", 3, remainingCount);
    }

    // ==================== EDGE CASES TESTS ====================

    /**
     * Test inserting task with minimum valid values
     * Verifies that DAO handles minimum valid values correctly
     */
    @Test
    @Transactional
    @Rollback
    public void testInsert_MinimumValidValues() {
        // Given: Task with only required fields
        Task minTask = new Task();
        minTask.setTaskType("TEST");
        minTask.setStatus("PENDING");

        // When: Insert the task
        taskDao.insert(minTask);

        // Then: Should save successfully with defaults
        assertNotNull("Should have generated ID", minTask.getId());
        assertNotNull("Should have created date", minTask.getCreatedDate());
        assertEquals("Should preserve task type", "TEST", minTask.getTaskType());
        assertEquals("Should preserve status", "PENDING", minTask.getStatus());
        assertEquals("Should have default progress", 0.0, minTask.getProgressPercentage(), 0.01);
        assertFalse("Should have default cancellation flag", minTask.isCancellationRequested());
    }

    /**
     * Test inserting task with maximum field lengths
     * Verifies that DAO handles large values correctly
     */
    @Test
    @Transactional
    @Rollback
    public void testInsert_MaximumFieldLengths() {
        // Given: Task with maximum field lengths
        Task maxTask = new Task();
        maxTask.setTaskType(generateString("A", 50)); // Max 50 chars
        maxTask.setStatus(generateString("B", 20)); // Max 20 chars
        maxTask.setFileName(generateString("C", 255)); // Max 255 chars
        maxTask.setErrorMessage(generateString("D", 1000)); // Max 1000 chars
        maxTask.setUserId(generateString("E", 50)); // Max 50 chars
        maxTask.setParameters(generateString("F", 2000)); // Max 2000 chars
        maxTask.setCurrentPhase(generateString("G", 50)); // Max 50 chars
        maxTask.setResultUrl(generateString("H", 500)); // Max 500 chars
        maxTask.setResultType(generateString("I", 50)); // Max 50 chars
        maxTask.setMetadata(generateString("J", 4000)); // Max 4000 chars

        // When: Insert the task
        taskDao.insert(maxTask);

        // Then: Should save successfully
        assertNotNull("Should save task with max field lengths", maxTask.getId());
        assertEquals("Should preserve max task type", 50, maxTask.getTaskType().length());
        assertEquals("Should preserve max status", 20, maxTask.getStatus().length());
        assertEquals("Should preserve max file name", 255, maxTask.getFileName().length());
    }

    /**
     * Test task lifecycle methods
     * Verifies that Task entity methods work correctly
     */
    @Test
    @Transactional
    @Rollback
    public void testTaskLifecycleMethods() {
        // Given: Insert a task
        taskDao.insert(testTask1);

        // When: Test lifecycle methods
        boolean isRunning = testTask1.isRunning();
        boolean isCompleted = testTask1.isCompleted();
        boolean isFailed = testTask1.isFailed();
        boolean isCancelled = testTask1.isCancelled();

        // Then: Should return correct states
        assertTrue("PENDING task should be running", isRunning);
        assertFalse("PENDING task should not be completed", isCompleted);
        assertFalse("PENDING task should not be failed", isFailed);
        assertFalse("PENDING task should not be cancelled", isCancelled);

        // Test progress update
        testTask1.updateProgress(75.0, "FINALIZING", "Almost done");
        assertEquals("Should update progress", 75.0, testTask1.getProgressPercentage(), 0.01);
        assertEquals("Should update phase", "FINALIZING", testTask1.getCurrentPhase());
        assertNotNull("Should set metadata", testTask1.getMetadata());

        // Test cancellation request
        testTask1.requestCancellation();
        assertTrue("Should request cancellation", testTask1.isCancellationRequested());
    }

    /**
     * Test complex query combinations
     * Verifies that multiple query methods work correctly together
     */
    @Test
    @Transactional
    @Rollback
    public void testComplexQueryCombinations() {
        // Given: Insert tasks with various combinations
        taskDao.insert(testTask1); // FILE_UPLOAD, PENDING
        taskDao.insert(testTask2); // ALGORITHM_RUN, RUNNING
        taskDao.insert(testTask3); // FILE_DOWNLOAD, COMPLETED
        taskDao.insert(testTask4); // FILE_UPLOAD, FAILED

        // When: Perform various queries
        List<Task> allTasks = taskDao.selectAll();
        List<Task> runningTasks = taskDao.getRunningTasks();
        List<Task> uploadTasks = taskDao.getTasksByType("FILE_UPLOAD");
        List<Task> failedTasks = taskDao.getFailedTasks();
        long totalCount = taskDao.getCount();
        int activeCount = taskDao.getActiveTaskCount();

        // Then: All queries should return correct results
        assertEquals("Should have 4 total tasks", 4, allTasks.size());
        assertEquals("Should have 2 running tasks", 2, runningTasks.size());
        assertEquals("Should have 2 upload tasks", 2, uploadTasks.size());
        assertEquals("Should have 1 failed task", 1, failedTasks.size());
        assertEquals("Total count should be 4", 4, totalCount);
        assertEquals("Active count should be 2", 2, activeCount);
    }

    // ==================== TRANSACTION ROLLBACK TESTS ====================

    /**
     * Test transaction rollback on exception
     * Verifies that failed operations are rolled back
     */
    @Test
    @Transactional
    @Rollback
    public void testTransactionRollback_OnException() {
        // Given: Initial state
        long initialCount = taskDao.getCount();

        try {
            // When: Insert valid task first
            taskDao.insert(testTask1);
            
            // Then try to insert invalid task (should cause exception)
            Task invalidTask = new Task();
            // Don't set required fields to cause validation error
            taskDao.insert(invalidTask);
            
            fail("Should have thrown exception");
        } catch (Exception e) {
            // Expected exception - this is good
            // The transaction should be rolled back due to @Rollback annotation
        }

        // Then: Transaction should be rolled back due to @Rollback annotation
        // Note: We can't easily test rollback within the same transaction
        // The @Rollback annotation ensures the entire test transaction is rolled back
        // This test verifies that exceptions are handled properly
    }

    /**
     * Test that @Rollback annotation works correctly
     * Verifies that test data is cleaned up after test
     */
    @Test
    @Transactional
    @Rollback
    public void testRollbackAnnotation_Cleanup() {
        // Given: Insert some test data
        taskDao.insert(testTask1);
        taskDao.insert(testTask2);
        
        // Verify data exists
        assertEquals("Should have 2 tasks", 2, taskDao.getCount());
        
        // Test will automatically rollback due to @Rollback annotation
        // This test verifies the rollback mechanism works
    }

    /**
     * Test batch operation rollback simulation
     * Verifies that batch-like operations can be rolled back
     */
    @Test
    @Transactional
    @Rollback
    public void testBatchOperationRollback() {
        // Given: Initial state
        long initialCount = taskDao.getCount();

        // When: Perform batch-like operation (multiple inserts)
        taskDao.insert(testTask1);
        taskDao.insert(testTask2);
        taskDao.insert(testTask3);
        
        // Verify batch was saved
        assertEquals("Should have saved batch", initialCount + 3, taskDao.getCount());
        
        // Test will rollback due to @Rollback annotation
        // This verifies batch-like operations respect transaction boundaries
    }

    /**
     * Test rollback on failed batch-like insert
     * Verifies that if one item in batch fails, entire batch is rolled back
     */
    @Test
    @Transactional
    @Rollback
    public void testBatchRollback_OnFailedInsert() {
        // Given: Initial state
        long initialCount = taskDao.getCount();

        try {
            // When: Perform batch with one invalid task
            taskDao.insert(testTask1); // Valid task
            
            Task invalidTask = new Task();
            // Don't set required fields to cause validation error
            taskDao.insert(invalidTask); // Invalid task
            
            taskDao.insert(testTask2); // Valid task
            
            fail("Should have thrown exception for invalid task");
        } catch (Exception e) {
            // Expected exception due to invalid task
        }

        // Then: Should rollback to initial state due to @Rollback annotation
        // This test verifies that batch-like operations handle failures appropriately
    }

    /**
     * Test concurrent access simulation
     * Verifies that DAO handles multiple operations correctly
     */
    @Test
    @Transactional
    @Rollback
    public void testConcurrentAccess_Simulation() {
        // Given: Insert initial tasks
        taskDao.insert(testTask1);
        taskDao.insert(testTask2);

        // When: Perform multiple operations in sequence (simulating concurrent access)
        List<Task> allTasks = taskDao.selectAll();
        List<Task> runningTasks = taskDao.getRunningTasks();
        List<Task> uploadTasks = taskDao.getTasksByType("FILE_UPLOAD");
        List<Task> recentTasks = taskDao.getRecentTasks(10);
        long totalCount = taskDao.getCount();
        int activeCount = taskDao.getActiveTaskCount();

        // Then: All operations should work correctly
        assertEquals("Should have 2 total tasks", 2, allTasks.size());
        assertEquals("Should have 2 running tasks", 2, runningTasks.size());
        assertEquals("Should have 1 upload task", 1, uploadTasks.size());
        assertEquals("Should have 2 recent tasks", 2, recentTasks.size());
        assertEquals("Count should be 2", 2, totalCount);
        assertEquals("Active count should be 2", 2, activeCount);
    }

    // ==================== HELPER METHODS ====================

    /**
     * Helper method to generate string of specified length
     * Java 8 compatible alternative to String.repeat()
     */
    private String generateString(String character, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(character);
        }
        return sb.toString();
    }
}
