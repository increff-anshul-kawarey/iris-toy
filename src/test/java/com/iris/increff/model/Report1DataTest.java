package com.iris.increff.model;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Date;

/**
 * Test class for Report1Data
 * Tests all getters and setters for report DTO
 */
public class Report1DataTest {

    @Test
    public void testReport1DataGettersAndSetters() {
        Report1Data data = new Report1Data();
        Date testDate = new Date();

        data.setExecutionDate(testDate);
        data.setAlgorithmLabel("Test Algorithm");
        data.setExecutionStatus("COMPLETED");
        data.setTotalStylesProcessed(500);
        data.setCoreStyles(200);
        data.setBestsellerStyles(150);
        data.setFashionStyles(150);
        data.setExecutionTimeMinutes(25.5);
        data.setParameters("test parameters");

        assertEquals(testDate, data.getExecutionDate());
        assertEquals("Test Algorithm", data.getAlgorithmLabel());
        assertEquals("COMPLETED", data.getExecutionStatus());
        assertEquals(Integer.valueOf(500), data.getTotalStylesProcessed());
        assertEquals(Integer.valueOf(200), data.getCoreStyles());
        assertEquals(Integer.valueOf(150), data.getBestsellerStyles());
        assertEquals(Integer.valueOf(150), data.getFashionStyles());
        assertEquals(Double.valueOf(25.5), data.getExecutionTimeMinutes());
        assertEquals("test parameters", data.getParameters());
    }

    @Test
    public void testReport1DataNoArgsConstructor() {
        Report1Data data = new Report1Data();
        assertNotNull(data);
        assertNull(data.getExecutionDate());
        assertNull(data.getAlgorithmLabel());
    }

    @Test
    public void testReport1DataWithDifferentExecutionStatuses() {
        String[] statuses = {"COMPLETED", "FAILED", "IN_PROGRESS"};

        for (String status : statuses) {
            Report1Data data = new Report1Data();
            data.setExecutionStatus(status);
            assertEquals(status, data.getExecutionStatus());
        }
    }

    @Test
    public void testReport1DataToString() {
        Report1Data data = new Report1Data();
        data.setAlgorithmLabel("TEST001");

        String toString = data.toString();
        assertNotNull(toString);
    }
}
