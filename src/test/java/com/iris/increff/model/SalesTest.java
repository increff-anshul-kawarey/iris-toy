package com.iris.increff.model;

import org.junit.Test;
import static org.junit.Assert.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Test class for Sales entity
 * Tests all getters, setters, and entity relationships
 */
public class SalesTest {

    @Test
    public void testSalesGettersAndSetters() {
        Sales sales = new Sales();
        Date testDate = new Date();

        sales.setId(1);
        sales.setDate(testDate);
        sales.setSkuId(10);
        sales.setStoreId(5);
        sales.setQuantity(100);
        sales.setDiscount(new BigDecimal("50.00"));
        sales.setRevenue(new BigDecimal("4500.00"));

        assertEquals(Integer.valueOf(1), sales.getId());
        assertEquals(testDate, sales.getDate());
        assertEquals(Integer.valueOf(10), sales.getSkuId());
        assertEquals(Integer.valueOf(5), sales.getStoreId());
        assertEquals(Integer.valueOf(100), sales.getQuantity());
        assertEquals(new BigDecimal("50.00"), sales.getDiscount());
        assertEquals(new BigDecimal("4500.00"), sales.getRevenue());
    }

    @Test
    public void testSalesAllArgsConstructor() {
        Date testDate = new Date();
        Sales sales = new Sales(1, testDate, 10, 5, 100,
                               new BigDecimal("50.00"), new BigDecimal("4500.00"), null, null);

        assertEquals(Integer.valueOf(1), sales.getId());
        assertEquals(testDate, sales.getDate());
        assertEquals(Integer.valueOf(10), sales.getSkuId());
        assertEquals(Integer.valueOf(5), sales.getStoreId());
        assertEquals(Integer.valueOf(100), sales.getQuantity());
    }

    @Test
    public void testSalesNoArgsConstructor() {
        Sales sales = new Sales();
        assertNotNull(sales);
        assertNull(sales.getId());
        assertNull(sales.getDate());
    }

    @Test
    public void testSalesWithRelationships() {
        Sales sales = new Sales();

        SKU sku = new SKU();
        sku.setId(1);
        sku.setSku("SKU001");

        Store store = new Store();
        store.setId(1);
        store.setBranch("Branch1");

        sales.setSku(sku);
        sales.setStore(store);

        assertNotNull(sales.getSku());
        assertNotNull(sales.getStore());
        assertEquals("SKU001", sales.getSku().getSku());
        assertEquals("Branch1", sales.getStore().getBranch());
    }

    @Test
    public void testSalesWithZeroDiscount() {
        Sales sales = new Sales();
        sales.setDiscount(new BigDecimal("0.00"));
        sales.setRevenue(new BigDecimal("1000.00"));

        assertEquals(new BigDecimal("0.00"), sales.getDiscount());
        assertEquals(new BigDecimal("1000.00"), sales.getRevenue());
    }

    @Test
    public void testSalesWithHighDiscount() {
        Sales sales = new Sales();
        sales.setDiscount(new BigDecimal("999.99"));

        assertEquals(new BigDecimal("999.99"), sales.getDiscount());
    }

    @Test
    public void testSalesEquality() {
        Date date = new Date();
        Sales sales1 = new Sales(1, date, 10, 5, 100,
                                new BigDecimal("50.00"), new BigDecimal("4500.00"), null, null);
        Sales sales2 = new Sales(1, date, 10, 5, 100,
                                new BigDecimal("50.00"), new BigDecimal("4500.00"), null, null);

        assertEquals(sales1, sales2);
        assertEquals(sales1.hashCode(), sales2.hashCode());
    }

    @Test
    public void testSalesToString() {
        Sales sales = new Sales(1, new Date(), 10, 5, 100,
                               new BigDecimal("50.00"), new BigDecimal("4500.00"), null, null);
        String toString = sales.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("100"));
    }
}
