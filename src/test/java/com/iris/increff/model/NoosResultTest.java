package com.iris.increff.model;

import org.junit.Test;
import static org.junit.Assert.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Test class for NoosResult entity
 * Tests all getters, setters, and validation
 */
public class NoosResultTest {

    @Test
    public void testNoosResultGettersAndSetters() {
        NoosResult result = new NoosResult();
        Date testDate = new Date();

        result.setId(1L);
        result.setCategory("Shirts");
        result.setStyleCode("STYLE001");
        result.setStyleROS(new BigDecimal("1.25"));
        result.setType("core");
        result.setStyleRevContribution(new BigDecimal("0.15"));
        result.setCalculatedDate(testDate);
        result.setTotalQuantitySold(500);
        result.setTotalRevenue(new BigDecimal("25000.00"));
        result.setDaysAvailable(90);
        result.setDaysWithSales(85);
        result.setAvgDiscount(new BigDecimal("0.10"));
        result.setAlgorithmRunId(100L);

        assertEquals(Long.valueOf(1), result.getId());
        assertEquals("Shirts", result.getCategory());
        assertEquals("STYLE001", result.getStyleCode());
        assertEquals(new BigDecimal("1.25"), result.getStyleROS());
        assertEquals("core", result.getType());
        assertEquals(new BigDecimal("0.15"), result.getStyleRevContribution());
        assertEquals(testDate, result.getCalculatedDate());
        assertEquals(Integer.valueOf(500), result.getTotalQuantitySold());
        assertEquals(new BigDecimal("25000.00"), result.getTotalRevenue());
        assertEquals(Integer.valueOf(90), result.getDaysAvailable());
        assertEquals(Integer.valueOf(85), result.getDaysWithSales());
        assertEquals(new BigDecimal("0.10"), result.getAvgDiscount());
        assertEquals(Long.valueOf(100), result.getAlgorithmRunId());
    }

    @Test
    public void testNoosResultAllArgsConstructor() {
        Date date = new Date();
        NoosResult result = new NoosResult(1L, "Pants", "STYLE002",
                                          new BigDecimal("2.50"), "bestseller",
                                          new BigDecimal("0.25"), date, 1000,
                                          new BigDecimal("50000.00"), 180, 175,
                                          new BigDecimal("0.15"), 200L);

        assertEquals(Long.valueOf(1), result.getId());
        assertEquals("Pants", result.getCategory());
        assertEquals("STYLE002", result.getStyleCode());
        assertEquals("bestseller", result.getType());
    }

    @Test
    public void testNoosResultNoArgsConstructor() {
        NoosResult result = new NoosResult();
        assertNotNull(result);
        assertNull(result.getId());
        assertNull(result.getCategory());
    }

    @Test
    public void testNoosResultCoreType() {
        NoosResult result = new NoosResult();
        result.setType("core");
        result.setStyleROS(new BigDecimal("1.50"));
        result.setStyleRevContribution(new BigDecimal("0.20"));

        assertEquals("core", result.getType());
        assertTrue(result.getStyleROS().compareTo(BigDecimal.ONE) > 0);
    }

    @Test
    public void testNoosResultBestsellerType() {
        NoosResult result = new NoosResult();
        result.setType("bestseller");
        result.setStyleROS(new BigDecimal("3.00"));
        result.setStyleRevContribution(new BigDecimal("0.30"));

        assertEquals("bestseller", result.getType());
        assertTrue(result.getStyleROS().compareTo(new BigDecimal("2.00")) > 0);
    }

    @Test
    public void testNoosResultFashionType() {
        NoosResult result = new NoosResult();
        result.setType("fashion");
        result.setStyleROS(new BigDecimal("0.50"));
        result.setStyleRevContribution(new BigDecimal("0.05"));

        assertEquals("fashion", result.getType());
    }

    @Test
    public void testNoosResultWithZeroValues() {
        NoosResult result = new NoosResult();
        result.setTotalQuantitySold(0);
        result.setAvgDiscount(new BigDecimal("0.00"));

        assertEquals(Integer.valueOf(0), result.getTotalQuantitySold());
        assertEquals(new BigDecimal("0.00"), result.getAvgDiscount());
    }

    @Test
    public void testNoosResultWithHighValues() {
        NoosResult result = new NoosResult();
        result.setTotalQuantitySold(999999);
        result.setTotalRevenue(new BigDecimal("9999999.99"));
        result.setStyleROS(new BigDecimal("999.9999"));

        assertEquals(Integer.valueOf(999999), result.getTotalQuantitySold());
        assertEquals(new BigDecimal("9999999.99"), result.getTotalRevenue());
        assertEquals(new BigDecimal("999.9999"), result.getStyleROS());
    }

    @Test
    public void testNoosResultEquality() {
        Date date = new Date();
        NoosResult r1 = new NoosResult(1L, "Cat1", "STYLE001", new BigDecimal("1.25"),
                                      "core", new BigDecimal("0.15"), date, 100,
                                      new BigDecimal("5000"), 90, 85, new BigDecimal("0.10"), 1L);
        NoosResult r2 = new NoosResult(1L, "Cat1", "STYLE001", new BigDecimal("1.25"),
                                      "core", new BigDecimal("0.15"), date, 100,
                                      new BigDecimal("5000"), 90, 85, new BigDecimal("0.10"), 1L);

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    public void testNoosResultToString() {
        NoosResult result = new NoosResult();
        result.setStyleCode("TEST001");
        result.setCategory("TestCat");
        result.setType("core");

        String toString = result.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("TEST001"));
    }
}