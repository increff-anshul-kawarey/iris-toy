package com.iris.increff.model;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Date;

/**
 * Test class for AlgorithmParameters entity
 * Tests all getters, setters, lifecycle hooks, and conversion methods
 */
public class AlgorithmParametersTest {

    @Test
    public void testAlgorithmParametersGettersAndSetters() {
        AlgorithmParameters params = new AlgorithmParameters();
        Date startDate = new Date();
        Date endDate = new Date(startDate.getTime() + 86400000L * 30); // 30 days later
        Date created = new Date();
        Date updated = new Date();

        params.setId(1L);
        params.setParameterSet("default");
        params.setLiquidationThreshold(0.25);
        params.setBestsellerMultiplier(1.20);
        params.setMinVolumeThreshold(25.0);
        params.setConsistencyThreshold(0.75);
        params.setDescription("Default parameters");
        params.setAnalysisStartDate(startDate);
        params.setAnalysisEndDate(endDate);
        params.setCoreDurationMonths(6);
        params.setBestsellerDurationDays(90);
        params.setIsActive(true);
        params.setCreatedDate(created);
        params.setLastUpdatedDate(updated);
        params.setUpdatedBy("admin");

        assertEquals(Long.valueOf(1), params.getId());
        assertEquals("default", params.getParameterSet());
        assertEquals(Double.valueOf(0.25), params.getLiquidationThreshold());
        assertEquals(Double.valueOf(1.20), params.getBestsellerMultiplier());
        assertEquals(Double.valueOf(25.0), params.getMinVolumeThreshold());
        assertEquals(Double.valueOf(0.75), params.getConsistencyThreshold());
        assertEquals("Default parameters", params.getDescription());
        assertEquals(startDate, params.getAnalysisStartDate());
        assertEquals(endDate, params.getAnalysisEndDate());
        assertEquals(Integer.valueOf(6), params.getCoreDurationMonths());
        assertEquals(Integer.valueOf(90), params.getBestsellerDurationDays());
        assertTrue(params.getIsActive());
        assertEquals(created, params.getCreatedDate());
        assertEquals(updated, params.getLastUpdatedDate());
        assertEquals("admin", params.getUpdatedBy());
    }

    @Test
    public void testAlgorithmParametersNoArgsConstructor() {
        AlgorithmParameters params = new AlgorithmParameters();
        assertNotNull(params);
        assertNull(params.getId());
        // Test default values
        assertEquals(Double.valueOf(0.25), params.getLiquidationThreshold());
        assertEquals(Double.valueOf(1.20), params.getBestsellerMultiplier());
        assertEquals(Double.valueOf(25.0), params.getMinVolumeThreshold());
        assertEquals(Double.valueOf(0.75), params.getConsistencyThreshold());
        assertEquals(Integer.valueOf(6), params.getCoreDurationMonths());
        assertEquals(Integer.valueOf(90), params.getBestsellerDurationDays());
        assertTrue(params.getIsActive());
    }

    @Test
    public void testAlgorithmParametersAllArgsConstructor() {
        Date start = new Date();
        Date end = new Date();
        Date created = new Date();
        Date updated = new Date();

        AlgorithmParameters params = new AlgorithmParameters(
            1L, "seasonal", 0.30, 1.50, 30.0, 0.80, "Seasonal params",
            start, end, 9, 120, true, created, updated, "user1"
        );

        assertEquals(Long.valueOf(1), params.getId());
        assertEquals("seasonal", params.getParameterSet());
        assertEquals(Double.valueOf(0.30), params.getLiquidationThreshold());
        assertEquals(Double.valueOf(1.50), params.getBestsellerMultiplier());
    }

    @Test
    public void testPrePersistHook() {
        AlgorithmParameters params = new AlgorithmParameters();
        params.setParameterSet("test");

        // Simulate @PrePersist
        params.onCreate();

        assertNotNull(params.getCreatedDate());
        assertNotNull(params.getLastUpdatedDate());
        assertEquals(params.getCreatedDate(), params.getLastUpdatedDate());
    }

    @Test
    public void testPreUpdateHook() throws InterruptedException {
        AlgorithmParameters params = new AlgorithmParameters();
        params.onCreate();

        Date originalUpdate = params.getLastUpdatedDate();
        Thread.sleep(10); // Small delay to ensure different timestamps

        // Simulate @PreUpdate
        params.onUpdate();

        assertNotNull(params.getLastUpdatedDate());
        assertTrue(params.getLastUpdatedDate().getTime() >= originalUpdate.getTime());
    }

    @Test
    public void testToAlgoParametersData() {
        Date start = new Date();
        Date end = new Date();
        Date updated = new Date();

        AlgorithmParameters params = new AlgorithmParameters();
        params.setId(1L);
        params.setParameterSet("test-set");
        params.setLiquidationThreshold(0.20);
        params.setBestsellerMultiplier(1.30);
        params.setMinVolumeThreshold(30.0);
        params.setConsistencyThreshold(0.70);
        params.setDescription("Test description");
        params.setAnalysisStartDate(start);
        params.setAnalysisEndDate(end);
        params.setCoreDurationMonths(12);
        params.setBestsellerDurationDays(60);
        params.setIsActive(true);
        params.setLastUpdatedDate(updated);

        AlgoParametersData data = params.toAlgoParametersData();

        assertNotNull(data);
        assertEquals("test-set", data.getParameterSetName());
        assertEquals(0.20, data.getLiquidationThreshold(), 0.001);
        assertEquals(1.30, data.getBestsellerMultiplier(), 0.001);
        assertEquals(30.0, data.getMinVolumeThreshold(), 0.001);
        assertEquals(0.70, data.getConsistencyThreshold(), 0.001);
        assertEquals("Test description", data.getAlgorithmLabel());
        assertEquals(start, data.getAnalysisStartDate());
        assertEquals(end, data.getAnalysisEndDate());
        assertEquals(Integer.valueOf(12), data.getCoreDurationMonths());
        assertEquals(Integer.valueOf(60), data.getBestsellerDurationDays());
        assertTrue(data.getIsActive());
        assertEquals(updated, data.getLastUpdated());
    }

    @Test
    public void testUpdateFromAlgoParametersData() {
        AlgorithmParameters params = new AlgorithmParameters();
        params.setId(1L);
        params.setParameterSet("original");

        AlgoParametersData data = new AlgoParametersData();
        data.setLiquidationThreshold(0.35);
        data.setBestsellerMultiplier(1.40);
        data.setMinVolumeThreshold(40.0);
        data.setConsistencyThreshold(0.85);
        data.setAlgorithmLabel("Updated description");
        data.setAnalysisStartDate(new Date());
        data.setAnalysisEndDate(new Date());
        data.setCoreDurationMonths(8);
        data.setBestsellerDurationDays(100);

        params.updateFromAlgoParametersData(data, "testUser");

        assertEquals(Double.valueOf(0.35), params.getLiquidationThreshold());
        assertEquals(Double.valueOf(1.40), params.getBestsellerMultiplier());
        assertEquals(Double.valueOf(40.0), params.getMinVolumeThreshold());
        assertEquals(Double.valueOf(0.85), params.getConsistencyThreshold());
        assertEquals("Updated description", params.getDescription());
        assertEquals(Integer.valueOf(8), params.getCoreDurationMonths());
        assertEquals(Integer.valueOf(100), params.getBestsellerDurationDays());
        assertEquals("testUser", params.getUpdatedBy());
    }

    @Test
    public void testActiveInactiveParameters() {
        AlgorithmParameters params = new AlgorithmParameters();

        params.setIsActive(true);
        assertTrue(params.getIsActive());

        params.setIsActive(false);
        assertFalse(params.getIsActive());
    }

    @Test
    public void testDifferentParameterSets() {
        String[] sets = {"default", "seasonal", "promotional", "test", "production"};

        for (String set : sets) {
            AlgorithmParameters params = new AlgorithmParameters();
            params.setParameterSet(set);
            assertEquals(set, params.getParameterSet());
        }
    }

    @Test
    public void testParameterBoundaryValues() {
        AlgorithmParameters params = new AlgorithmParameters();

        // Test minimum values
        params.setLiquidationThreshold(0.0);
        params.setBestsellerMultiplier(1.0);
        params.setMinVolumeThreshold(0.0);
        params.setConsistencyThreshold(0.0);

        assertEquals(Double.valueOf(0.0), params.getLiquidationThreshold());
        assertEquals(Double.valueOf(1.0), params.getBestsellerMultiplier());

        // Test maximum values
        params.setLiquidationThreshold(1.0);
        params.setBestsellerMultiplier(5.0);
        params.setMinVolumeThreshold(1000.0);
        params.setConsistencyThreshold(1.0);

        assertEquals(Double.valueOf(1.0), params.getLiquidationThreshold());
        assertEquals(Double.valueOf(5.0), params.getBestsellerMultiplier());
    }

    @Test
    public void testEquality() {
        Date date = new Date();
        AlgorithmParameters p1 = new AlgorithmParameters(
            1L, "test", 0.25, 1.20, 25.0, 0.75, "desc",
            date, date, 6, 90, true, date, date, "user"
        );
        AlgorithmParameters p2 = new AlgorithmParameters(
            1L, "test", 0.25, 1.20, 25.0, 0.75, "desc",
            date, date, 6, 90, true, date, date, "user"
        );

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    public void testToString() {
        AlgorithmParameters params = new AlgorithmParameters();
        params.setParameterSet("test");
        params.setDescription("Test params");

        String toString = params.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("test"));
    }
}

