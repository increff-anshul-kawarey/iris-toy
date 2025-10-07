package com.iris.increff.model;

import org.junit.Test;
import static org.junit.Assert.*;
import java.math.BigDecimal;

/**
 * Test class for Style entity
 * Tests all getters, setters, and entity behavior
 */
public class StyleTest {

    @Test
    public void testStyleGettersAndSetters() {
        Style style = new Style();

        // Test all setters and getters
        style.setId(1);
        style.setStyleCode("STYLE001");
        style.setBrand("Nike");
        style.setCategory("Shoes");
        style.setSubCategory("Running");
        style.setMrp(new BigDecimal("999.99"));
        style.setGender("Male");

        assertEquals(Integer.valueOf(1), style.getId());
        assertEquals("STYLE001", style.getStyleCode());
        assertEquals("Nike", style.getBrand());
        assertEquals("Shoes", style.getCategory());
        assertEquals("Running", style.getSubCategory());
        assertEquals(new BigDecimal("999.99"), style.getMrp());
        assertEquals("Male", style.getGender());
    }

    @Test
    public void testStyleAllArgsConstructor() {
        Style style = new Style(1, "STYLE002", "Adidas", "Apparel", "Tshirt",
                               new BigDecimal("499.50"), "Female");

        assertEquals(Integer.valueOf(1), style.getId());
        assertEquals("STYLE002", style.getStyleCode());
        assertEquals("Adidas", style.getBrand());
        assertEquals("Apparel", style.getCategory());
        assertEquals("Tshirt", style.getSubCategory());
        assertEquals(new BigDecimal("499.50"), style.getMrp());
        assertEquals("Female", style.getGender());
    }

    @Test
    public void testStyleNoArgsConstructor() {
        Style style = new Style();
        assertNotNull(style);
        assertNull(style.getId());
        assertNull(style.getStyleCode());
    }

    @Test
    public void testStyleWithMinMaxValues() {
        Style style = new Style();
        style.setMrp(new BigDecimal("0.01")); // Minimum valid MRP
        assertEquals(new BigDecimal("0.01"), style.getMrp());

        style.setMrp(new BigDecimal("999999.99")); // Large MRP
        assertEquals(new BigDecimal("999999.99"), style.getMrp());
    }

    @Test
    public void testStyleWithEmptyStrings() {
        Style style = new Style();
        style.setStyleCode("");
        style.setBrand("");
        style.setCategory("");
        style.setSubCategory("");
        style.setGender("");

        assertEquals("", style.getStyleCode());
        assertEquals("", style.getBrand());
    }

    @Test
    public void testStyleEquality() {
        Style style1 = new Style(1, "STYLE001", "Brand1", "Cat1", "SubCat1",
                                new BigDecimal("100"), "M");
        Style style2 = new Style(1, "STYLE001", "Brand1", "Cat1", "SubCat1",
                                new BigDecimal("100"), "M");

        // Lombok @Data generates equals and hashCode
        assertEquals(style1, style2);
        assertEquals(style1.hashCode(), style2.hashCode());
    }

    @Test
    public void testStyleToString() {
        Style style = new Style(1, "STYLE001", "Brand1", "Cat1", "SubCat1",
                               new BigDecimal("100"), "M");
        String toString = style.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("STYLE001"));
        assertTrue(toString.contains("Brand1"));
    }
}

