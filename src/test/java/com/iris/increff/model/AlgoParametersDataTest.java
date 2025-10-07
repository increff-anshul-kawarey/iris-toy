package com.iris.increff.model;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Date;

/**
 * Test class for AlgoParametersData
 * Tests all getters and setters for DTO
 */
public class AlgoParametersDataTest {

    @Test
    public void testAlgoParametersDataGettersAndSetters() {
        AlgoParametersData data = new AlgoParametersData();
        Date start = new Date();
        Date end = new Date();
        Date updated = new Date();

        data.setParameterSetName("default");
        data.setLiquidationThreshold(0.25);
        data.setBestsellerMultiplier(1.20);
        data.setMinVolumeThreshold(25.0);
        data.setConsistencyThreshold(0.75);
        data.setAlgorithmLabel("Default algorithm");
        data.setAnalysisStartDate(start);
        data.setAnalysisEndDate(end);
        data.setCoreDurationMonths(6);
        data.setBestsellerDurationDays(90);
        data.setIsActive(true);
        data.setLastUpdated(updated);

        assertEquals("default", data.getParameterSetName());
        assertEquals(0.25, data.getLiquidationThreshold(), 0.001);
        assertEquals(1.20, data.getBestsellerMultiplier(), 0.001);
        assertEquals(25.0, data.getMinVolumeThreshold(), 0.001);
        assertEquals(0.75, data.getConsistencyThreshold(), 0.001);
        assertEquals("Default algorithm", data.getAlgorithmLabel());
        assertEquals(start, data.getAnalysisStartDate());
        assertEquals(end, data.getAnalysisEndDate());
        assertEquals(Integer.valueOf(6), data.getCoreDurationMonths());
        assertEquals(Integer.valueOf(90), data.getBestsellerDurationDays());
        assertTrue(data.getIsActive());
        assertEquals(updated, data.getLastUpdated());
    }

    @Test
    public void testAlgoParametersDataNoArgsConstructor() {
        AlgoParametersData data = new AlgoParametersData();
        assertNotNull(data);
        assertNull(data.getParameterSetName());
        assertEquals(0.0, data.getLiquidationThreshold(), 0.001);
    }

    @Test
    public void testAlgoParametersDataWithNullValues() {
        AlgoParametersData data = new AlgoParametersData();
        data.setParameterSetName(null);
        data.setAlgorithmLabel(null);
        data.setAnalysisStartDate(null);
        data.setAnalysisEndDate(null);

        assertNull(data.getParameterSetName());
        assertNull(data.getAlgorithmLabel());
        assertNull(data.getAnalysisStartDate());
        assertNull(data.getAnalysisEndDate());
    }

    @Test
    public void testAlgoParametersDataToString() {
        AlgoParametersData data = new AlgoParametersData();
        data.setParameterSetName("test");

        String toString = data.toString();
        assertNotNull(toString);
        // Just verify toString returns a non-empty string
        assertTrue(toString.length() > 0);
    }
}

