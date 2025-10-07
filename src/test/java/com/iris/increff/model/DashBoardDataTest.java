package com.iris.increff.model;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test class for DashBoardData
 * Tests all getters and setters for dashboard DTO
 */
public class DashBoardDataTest {

    @Test
    public void testDashBoardDataGettersAndSetters() {
        DashBoardData data = new DashBoardData();

        // Tile 1: Data Records
        data.setTotalSalesRecords(100000L);
        data.setSalesDataStatus("LOADED");

        // Tile 2: Master Data
        data.setTotalSkus(5000L);
        data.setTotalStores(50L);
        data.setTotalStyles(1000L);
        data.setMasterDataStatus("COMPLETE");

        // Tile 3: Recent Activity
        data.setRecentUploads(15);
        data.setUploadSuccessRate(0.95);
        data.setRecentActivityStatus("ACTIVE");

        // Tile 4: Processing Status
        data.setActiveTasks(5);
        data.setPendingTasks(10);
        data.setProcessingStatus("RUNNING");

        // Verify all getters
        assertEquals(100000L, data.getTotalSalesRecords());
        assertEquals("LOADED", data.getSalesDataStatus());
        assertEquals(5000L, data.getTotalSkus());
        assertEquals(50L, data.getTotalStores());
        assertEquals(1000L, data.getTotalStyles());
        assertEquals("COMPLETE", data.getMasterDataStatus());
        assertEquals(15, data.getRecentUploads());
        assertEquals(0.95, data.getUploadSuccessRate(), 0.001);
        assertEquals("ACTIVE", data.getRecentActivityStatus());
        assertEquals(5, data.getActiveTasks());
        assertEquals(10, data.getPendingTasks());
        assertEquals("RUNNING", data.getProcessingStatus());
    }

    @Test
    public void testDashBoardDataNoArgsConstructor() {
        DashBoardData data = new DashBoardData();
        assertNotNull(data);
        assertEquals(0L, data.getTotalStyles());
        assertNull(data.getSalesDataStatus());
    }

    @Test
    public void testDashBoardDataWithZeroValues() {
        DashBoardData data = new DashBoardData();
        data.setTotalStyles(0L);
        data.setTotalSalesRecords(0L);
        data.setUploadSuccessRate(0.0);

        assertEquals(0L, data.getTotalStyles());
        assertEquals(0L, data.getTotalSalesRecords());
        assertEquals(0.0, data.getUploadSuccessRate(), 0.001);
    }

    @Test
    public void testDashBoardDataToString() {
        DashBoardData data = new DashBoardData();
        data.setTotalStyles(100L);

        String toString = data.toString();
        assertNotNull(toString);
    }
}
