package com.iris.increff.model;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test class for SKU entity
 * Tests all getters, setters, and entity relationships
 */
public class SKUTest {

    @Test
    public void testSKUGettersAndSetters() {
        SKU sku = new SKU();

        sku.setId(1);
        sku.setSku("ABC123");
        sku.setStyleId(10);
        sku.setSize("XL");

        assertEquals(Integer.valueOf(1), sku.getId());
        assertEquals("ABC123", sku.getSku());
        assertEquals(Integer.valueOf(10), sku.getStyleId());
        assertEquals("XL", sku.getSize());
    }

    @Test
    public void testSKUAllArgsConstructor() {
        Style style = new Style();
        style.setId(20);
        SKU sku = new SKU(2, "DEF456", 20, "M", style);

        assertEquals(Integer.valueOf(2), sku.getId());
        assertEquals("DEF456", sku.getSku());
        assertEquals(Integer.valueOf(20), sku.getStyleId());
        assertEquals("M", sku.getSize());
        assertNotNull(sku.getStyle());
    }

    @Test
    public void testSKUNoArgsConstructor() {
        SKU sku = new SKU();
        assertNotNull(sku);
        assertNull(sku.getId());
        assertNull(sku.getSku());
        assertNull(sku.getStyleId());
        assertNull(sku.getSize());
    }

    @Test
    public void testSKUWithStyleRelationship() {
        SKU sku = new SKU();
        Style style = new Style();
        style.setId(1);
        style.setStyleCode("STYLE001");

        sku.setStyle(style);
        sku.setStyleId(1);

        assertNotNull(sku.getStyle());
        assertEquals("STYLE001", sku.getStyle().getStyleCode());
    }

    @Test
    public void testSKUWithVariousSizes() {
        String[] sizes = {"XS", "S", "M", "L", "XL", "XXL", "XXXL"};

        for (String size : sizes) {
            SKU sku = new SKU();
            sku.setSize(size);
            assertEquals(size, sku.getSize());
        }
    }

    @Test
    public void testSKUEquality() {
        SKU sku1 = new SKU(1, "ABC123", 10, "L", null);
        SKU sku2 = new SKU(1, "ABC123", 10, "L", null);

        assertEquals(sku1, sku2);
        assertEquals(sku1.hashCode(), sku2.hashCode());
    }

    @Test
    public void testSKUToString() {
        SKU sku = new SKU(1, "TEST123", 5, "M", null);
        String toString = sku.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("TEST123"));
        assertTrue(toString.contains("M"));
    }
}
