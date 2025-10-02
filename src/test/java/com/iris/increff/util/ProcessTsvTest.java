package com.iris.increff.util;

import com.iris.increff.AbstractUnitTest;
import com.iris.increff.model.NoosResult;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ProcessTsvTest extends AbstractUnitTest {

    // =================================================================
    // Test createNoosResultsTsv
    // =================================================================

    @Test
    public void testCreateNoosResultsTsv() throws IOException {
        List<NoosResult> results = new ArrayList<>();
        Date now = new Date();
        NoosResult r1 = new NoosResult();
        r1.setCategory("SHIRTS");
        r1.setStyleCode("STYLE001");
        r1.setStyleROS(new BigDecimal("1.25"));
        r1.setType("core");
        r1.setStyleRevContribution(new BigDecimal("0.15"));
        r1.setTotalQuantitySold(100);
        r1.setTotalRevenue(new BigDecimal("5000.00"));
        r1.setDaysAvailable(90);
        r1.setDaysWithSales(85);
        r1.setAvgDiscount(new BigDecimal("0.10"));
        r1.setCalculatedDate(now);
        results.add(r1);

        MockHttpServletResponse response = new MockHttpServletResponse();
        ProcessTsv.createNoosResultsTsv(results, response);

        assertEquals("text/tab-separated-values", response.getContentType());
        assertTrue(response.getHeader("Content-Disposition").startsWith("attachment; filename=noos_results_"));

        String tsvOutput = response.getContentAsString();
        String[] lines = tsvOutput.split("\n");
        assertEquals(2, lines.length); // Header + 1 data row
        assertTrue(lines[0].startsWith("Category\tStyle Code"));
        assertTrue(lines[1].contains("SHIRTS\tSTYLE001\t1.25"));
        assertTrue(lines[1].contains(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(now)));
    }
}
