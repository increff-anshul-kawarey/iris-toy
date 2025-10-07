package com.iris.increff.model;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Date;

/**
 * Test class for Report2Data
 * Tests all getters and setters for report DTO
 */
public class Report2DataTest {

    @Test
    public void testReport2DataGettersAndSetters() {
        Report2Data data = new Report2Data();
        Date testDate = new Date();

        data.setDate(testDate);
        data.setTaskType("UPLOAD_SALES");
        data.setTotalTasks(200);
        data.setSuccessfulTasks(180);
        data.setFailedTasks(20);
        data.setSuccessRate(0.90);
        data.setAverageExecutionTime(15.5);
        data.setSystemStatus("HEALTHY");

        assertEquals(testDate, data.getDate());
        assertEquals("UPLOAD_SALES", data.getTaskType());
        assertEquals(Integer.valueOf(200), data.getTotalTasks());
        assertEquals(Integer.valueOf(180), data.getSuccessfulTasks());
        assertEquals(Integer.valueOf(20), data.getFailedTasks());
        assertEquals(Double.valueOf(0.90), data.getSuccessRate());
        assertEquals(Double.valueOf(15.5), data.getAverageExecutionTime());
        assertEquals("HEALTHY", data.getSystemStatus());
    }

    @Test
    public void testReport2DataNoArgsConstructor() {
        Report2Data data = new Report2Data();
        assertNotNull(data);
        assertNull(data.getDate());
        assertNull(data.getTaskType());
    }

    @Test
    public void testReport2DataWithVariousTaskTypes() {
        String[] taskTypes = {"UPLOAD_SALES", "UPLOAD_STYLES", "UPLOAD_SKU", "UPLOAD_STORES", "NOOS_ANALYSIS"};

        for (int i = 0; i < taskTypes.length; i++) {
            Report2Data data = new Report2Data();
            data.setTaskType(taskTypes[i]);
            data.setTotalTasks(i + 10);

            assertEquals(taskTypes[i], data.getTaskType());
            assertEquals(Integer.valueOf(i + 10), data.getTotalTasks());
        }
    }

    @Test
    public void testReport2DataToString() {
        Report2Data data = new Report2Data();
        data.setTaskType("TEST_TASK");
        data.setSystemStatus("HEALTHY");

        String toString = data.toString();
        assertNotNull(toString);
    }
}
