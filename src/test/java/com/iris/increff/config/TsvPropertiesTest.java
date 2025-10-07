package com.iris.increff.config;

import com.iris.increff.AbstractUnitTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * Test class for TsvProperties configuration
 * Tests that properties are correctly loaded from configuration files
 */
public class TsvPropertiesTest extends AbstractUnitTest {

    @Autowired
    private TsvProperties tsvProperties;

    @Test
    public void testTsvPropertiesIsLoaded() {
        assertNotNull("TsvProperties should be autowired", tsvProperties);
    }

    @Test
    public void testStylesHeadersAreLoaded() {
        String[] stylesHeaders = tsvProperties.getStylesHeaders();
        assertNotNull("Styles headers should not be null", stylesHeaders);
        assertTrue("Styles headers should contain elements", stylesHeaders.length > 0);
    }

    @Test
    public void testSkuHeadersAreLoaded() {
        String[] skuHeaders = tsvProperties.getSkuHeaders();
        assertNotNull("SKU headers should not be null", skuHeaders);
        assertTrue("SKU headers should contain elements", skuHeaders.length > 0);
    }

    @Test
    public void testStoreHeadersAreLoaded() {
        String[] storeHeaders = tsvProperties.getStoreHeaders();
        assertNotNull("Store headers should not be null", storeHeaders);
        assertTrue("Store headers should contain elements", storeHeaders.length > 0);
    }

    @Test
    public void testSalesHeadersAreLoaded() {
        String[] salesHeaders = tsvProperties.getSalesHeaders();
        assertNotNull("Sales headers should not be null", salesHeaders);
        assertTrue("Sales headers should contain elements", salesHeaders.length > 0);
    }

    @Test
    public void testPriceBucketHeadersAreLoaded() {
        String[] priceBucketHeaders = tsvProperties.getPriceBucketHeaders();
        assertNotNull("Price bucket headers should not be null", priceBucketHeaders);
        assertTrue("Price bucket headers should contain elements", priceBucketHeaders.length > 0);
    }

    @Test
    public void testStylesHeadersExpectedValues() {
        String[] stylesHeaders = tsvProperties.getStylesHeaders();
        // Verify that headers contain expected columns
        boolean hasStyleCode = false;
        for (String header : stylesHeaders) {
            if (header != null && (header.contains("Style") || header.contains("style"))) {
                hasStyleCode = true;
                break;
            }
        }
        assertTrue("Styles headers should contain style-related column", hasStyleCode);
    }

    @Test
    public void testAllHeaderArraysAreNotEmpty() {
        assertTrue("Styles headers should not be empty", tsvProperties.getStylesHeaders().length > 0);
        assertTrue("SKU headers should not be empty", tsvProperties.getSkuHeaders().length > 0);
        assertTrue("Store headers should not be empty", tsvProperties.getStoreHeaders().length > 0);
        assertTrue("Sales headers should not be empty", tsvProperties.getSalesHeaders().length > 0);
        assertTrue("Price bucket headers should not be empty", tsvProperties.getPriceBucketHeaders().length > 0);
    }
}

