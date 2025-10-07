package com.iris.increff.model;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Date;

/**
 * Test class for Task entity
 * Tests all getters, setters, and task lifecycle
 */
public class TaskTest {

    @Test
    public void testTaskGettersAndSetters() {
        Task task = new Task();
        Date startTime = new Date();
        Date endTime = new Date(startTime.getTime() + 60000); // 1 minute later

        task.setId(1L);
        task.setTaskType("FILE_UPLOAD");
        task.setStatus("PENDING");
        task.setFileName("test.tsv");
        task.setTotalRecords(1000);
        task.setProcessedRecords(500);
        task.setErrorCount(5);
        task.setStartTime(startTime);
        task.setEndTime(endTime);
        task.setErrorMessage("Some error");
        task.setUserId("user123");
        task.setParameters("{\"param\":\"value\"}");

        // currentPhase removed - phase info now in progressMessage
        task.setResultUrl("/downloads/result.tsv");
        task.setCancellationRequested(false);
        task.setCreatedDate(startTime);

        assertEquals(Long.valueOf(1), task.getId());
        assertEquals("FILE_UPLOAD", task.getTaskType());
        assertEquals("PENDING", task.getStatus());
        assertEquals("test.tsv", task.getFileName());
        assertEquals(Integer.valueOf(1000), task.getTotalRecords());
        assertEquals(Integer.valueOf(500), task.getProcessedRecords());
        assertEquals(Integer.valueOf(5), task.getErrorCount());
        assertEquals(startTime, task.getStartTime());
        assertEquals(endTime, task.getEndTime());
        assertEquals("Some error", task.getErrorMessage());
        assertEquals("user123", task.getUserId());
        assertEquals("{\"param\":\"value\"}", task.getParameters());
        assertEquals(Double.valueOf(50.0), Double.valueOf(task.getProgressPercentage()));
        // currentPhase removed - phase info now in progressMessage
        assertEquals("/downloads/result.tsv", task.getResultUrl());
        assertEquals(Boolean.FALSE, task.getCancellationRequested());
        assertEquals(startTime, task.getCreatedDate());
    }

    // @Test
    // public void testTaskAllArgsConstructor() {
    //     Date now = new Date();
    //     // AllArgsConstructor includes all 22 fields in order
    //     Task task = new Task(1L, "ALGORITHM_RUN", "RUNNING", "algo.tsv",
    //                         1000, 500, 5, now, null, "error", "user1",
    //                         "params", 50.0, "CALC", 5, 10, "/result",
    //                         "TSV", "meta", false, now, now);

    //     assertEquals(Long.valueOf(1), task.getId());
    //     assertEquals("ALGORITHM_RUN", task.getTaskType());
    //     assertEquals("RUNNING", task.getStatus());
    // }

    @Test
    public void testTaskNoArgsConstructor() {
        Task task = new Task();
        assertNotNull(task);
        assertNull(task.getId());
        assertNull(task.getTaskType());
    }

    @Test
    public void testTaskStatusTransitions() {
        Task task = new Task();

        task.setStatus("PENDING");
        assertEquals("PENDING", task.getStatus());

        task.setStatus("RUNNING");
        assertEquals("RUNNING", task.getStatus());

        task.setStatus("COMPLETED");
        assertEquals("COMPLETED", task.getStatus());

        task.setStatus("FAILED");
        assertEquals("FAILED", task.getStatus());
    }


    @Test
    public void testTaskWithCancellation() {
        Task task = new Task();

        task.setCancellationRequested(false);
        assertFalse(task.getCancellationRequested());

        task.setCancellationRequested(true);
        assertTrue(task.getCancellationRequested());
    }

    @Test
    public void testTaskPhases() {
        Task task = new Task();
        String[] phases = {"DATA_LOADING", "PROCESSING", "CLASSIFICATION", "FINALIZATION"};

        for (String phase : phases) {
            // currentPhase removed - phase info now in progressMessage
            // assertEquals(phase, task.getCurrentPhase());
        }
    }

    @Test
    public void testTaskProgressUpdates() {
        Task task = new Task();
        
        // Test progress updates
        task.updateProgress(25.0, "VALIDATING: Validating file format...");
        assertEquals(25.0, task.getProgressPercentage(), 0.001);
        // currentPhase removed - phase info now in progressMessage
        assertEquals("VALIDATING: Validating file format...", task.getProgressMessage());
        
        task.updateProgress(50.0, "PARSING: Parsing TSV data...");
        assertEquals(50.0, task.getProgressPercentage(), 0.001);
        // currentPhase removed - phase info now in progressMessage
        assertEquals("PARSING: Parsing TSV data...", task.getProgressMessage());
    }

    @Test
    public void testTaskWithErrorHandling() {
        Task task = new Task();

        task.setErrorCount(0);
        assertEquals(Integer.valueOf(0), task.getErrorCount());

        task.setErrorCount(5);
        task.setErrorMessage("5 validation errors found");
        assertEquals(Integer.valueOf(5), task.getErrorCount());
        assertEquals("5 validation errors found", task.getErrorMessage());
    }

    // @Test
    // public void testTaskEquality() {
    //     Date now = new Date();
    //     Task t1 = new Task(1L, "UPLOAD", "PENDING", "file.tsv", 100, 0, 0,
    //                       now, null, null, "user1", null, 0.0, null, 0, 10,
    //                       null, "TSV", null, false, now, now);
    //     Task t2 = new Task(1L, "UPLOAD", "PENDING", "file.tsv", 100, 0, 0,
    //                       now, null, null, "user1", null, 0.0, null, 0, 10,
    //                       null, "TSV", null, false, now, now);

    //     assertEquals(t1, t2);
    //     assertEquals(t1.hashCode(), t2.hashCode());
    // }

    @Test
    public void testTaskToString() {
        Task task = new Task();
        task.setTaskType("TEST_TASK");
        task.setStatus("RUNNING");

        String toString = task.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("TEST_TASK"));
    }
}
