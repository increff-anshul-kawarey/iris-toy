package com.iris.increff.util;

import com.iris.increff.AbstractUnitTest;
import com.iris.increff.model.AlgoParametersData;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import static org.junit.Assert.*;

public class TempDataCreatorTest extends AbstractUnitTest {

    @Test
    public void testGetAlgoParameters() throws ParseException {
        AlgoParametersData params = TempDataCreator.getAlgoParameters();

        assertNotNull(params);
        assertEquals(0.25, params.getLiquidationThreshold(), 0.001);
        assertEquals(1.20, params.getBestsellerMultiplier(), 0.001);
        assertEquals(25.0, params.getMinVolumeThreshold(), 0.001);
        assertEquals(0.75, params.getConsistencyThreshold(), 0.001);
        assertEquals("default_config", params.getAlgorithmLabel());
        assertEquals(6, (int) params.getCoreDurationMonths());
        assertEquals(90, (int) params.getBestsellerDurationDays());

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        assertEquals("2019-01-01", dateFormat.format(params.getAnalysisStartDate()));
        assertEquals("2019-06-23", dateFormat.format(params.getAnalysisEndDate()));
    }
}
