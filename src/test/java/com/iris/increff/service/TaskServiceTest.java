package com.iris.increff.service;

import com.iris.increff.AbstractUnitTest;
import com.iris.increff.dao.TaskDao;
import com.iris.increff.model.Task;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

import static org.junit.Assert.*;

/**
 * Test class for TaskService
 * Tests task creation and management with proper transaction handling
 */
public class TaskServiceTest extends AbstractUnitTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskDao taskDao;

    @Test
    public void testCreateTaskInNewTransaction() throws Exception {
        // Create a new task
        Task task = new Task();
        task.setTaskType("FILE_UPLOAD");
        task.setStatus("PENDING");
        task.setFileName("test.tsv");
        task.setTotalRecords(1000);
        task.setProcessedRecords(0);
        task.setErrorCount(0);
        task.setStartTime(new Date());

        task.setCreatedDate(new Date());
        task.setCancellationRequested(false);

        // Test createTaskInNewTransaction
        Task created = taskService.createTaskInNewTransaction(task);

        // Verify task was created and has an ID
        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("FILE_UPLOAD", created.getTaskType());
        assertEquals("PENDING", created.getStatus());
        assertEquals("test.tsv", created.getFileName());

        // Verify it's persisted in database
        Task retrieved = taskDao.select(created.getId());
        assertNotNull(retrieved);
        assertEquals(created.getId(), retrieved.getId());
    }

    @Test
    public void testUpdateTask() throws Exception {
        // Create initial task
        Task task = new Task();
        task.setTaskType("ALGORITHM_RUN");
        task.setStatus("PENDING");

        task.setCreatedDate(new Date());
        task.setCancellationRequested(false);

        taskDao.insert(task);

        // Update task
        task.setStatus("RUNNING");

        // currentPhase removed - phase info now in progressMessage
        task.setProcessedRecords(500);

        taskService.updateTask(task);

        // Verify update
        Task retrieved = taskDao.select(task.getId());
        assertNotNull(retrieved);
        assertEquals("RUNNING", retrieved.getStatus());
        assertEquals(50.0, retrieved.getProgressPercentage(), 0.001);
        // currentPhase removed - phase info now in progressMessage
        assertEquals(Integer.valueOf(500), retrieved.getProcessedRecords());
    }

    @Test
    public void testGetTask() throws Exception {
        // Create task
        Task task = new Task();
        task.setTaskType("FILE_DOWNLOAD");
        task.setStatus("COMPLETED");
        task.setFileName("download.tsv");
        task.setCreatedDate(new Date());
        task.setCancellationRequested(false);

        taskDao.insert(task);

        // Test getTask
        Task retrieved = taskService.getTask(task.getId());
        assertNotNull(retrieved);
        assertEquals(task.getId(), retrieved.getId());
        assertEquals("FILE_DOWNLOAD", retrieved.getTaskType());
        assertEquals("COMPLETED", retrieved.getStatus());
    }

    @Test
    public void testGetTaskNotFound() throws Exception {
        // Test getting non-existent task
        Task retrieved = taskService.getTask(999999L);
        assertNull(retrieved);
    }

    @Test
    public void testCreateTaskWithAllFields() throws Exception {
        Date now = new Date();

        Task task = new Task();
        task.setTaskType("FILE_UPLOAD");
        task.setStatus("PENDING");
        task.setFileName("styles.tsv");
        task.setTotalRecords(5000);
        task.setProcessedRecords(0);
        task.setErrorCount(0);
        task.setStartTime(now);
        task.setUserId("testUser");
        task.setParameters("{\"type\":\"styles\"}");

        // currentPhase removed - phase info now in progressMessage
//         task.setCurrentStep(1);
//         task.setTotalSteps(5);
//         task.setResultType("TSV");
//         task.setMetadata("{\"source\":\"ui\"}");
        task.setCancellationRequested(false);
        task.setCreatedDate(now);

        Task created = taskService.createTaskInNewTransaction(task);

        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("FILE_UPLOAD", created.getTaskType());
        assertEquals("testUser", created.getUserId());
        assertEquals("{\"type\":\"styles\"}", created.getParameters());
    }

    @Test
    public void testTaskLifecycle() throws Exception {
        // Create task
        Task task = new Task();
        task.setTaskType("ALGORITHM_RUN");
        task.setStatus("PENDING");

        task.setCreatedDate(new Date());
        task.setCancellationRequested(false);

        Task created = taskService.createTaskInNewTransaction(task);
        assertNotNull(created.getId());

        // Update to RUNNING
        created.setStatus("RUNNING");
        created.setStartTime(new Date());

        taskService.updateTask(created);

        Task running = taskService.getTask(created.getId());
        assertEquals("RUNNING", running.getStatus());
        assertEquals(25.0, running.getProgressPercentage(), 0.001);

        // Update progress

        running.setProcessedRecords(750);
        taskService.updateTask(running);

        Task inProgress = taskService.getTask(created.getId());
        assertEquals(75.0, inProgress.getProgressPercentage(), 0.001);

        // Complete task
        inProgress.setStatus("COMPLETED");

        inProgress.setEndTime(new Date());
        inProgress.setProcessedRecords(1000);
        taskService.updateTask(inProgress);

        Task completed = taskService.getTask(created.getId());
        assertEquals("COMPLETED", completed.getStatus());
        assertEquals(100.0, completed.getProgressPercentage(), 0.001);
        assertNotNull(completed.getEndTime());
    }

    @Test
    public void testTaskCancellation() throws Exception {
        // Create task
        Task task = new Task();
        task.setTaskType("FILE_UPLOAD");
        task.setStatus("RUNNING");
        task.setCancellationRequested(false);
        task.setCreatedDate(new Date());

        Task created = taskService.createTaskInNewTransaction(task);

        // Request cancellation
        created.setCancellationRequested(true);
        taskService.updateTask(created);

        Task retrieved = taskService.getTask(created.getId());
        assertTrue(retrieved.getCancellationRequested());
    }
}
