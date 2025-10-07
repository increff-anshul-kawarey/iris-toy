package com.iris.increff.model;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test class for Store entity
 * Tests all getters, setters, and entity behavior
 */
public class StoreTest {

    @Test
    public void testStoreGettersAndSetters() {
        Store store = new Store();

        store.setId(1);
        store.setBranch("Downtown Store");
        store.setCity("New York");

        assertEquals(Integer.valueOf(1), store.getId());
        assertEquals("Downtown Store", store.getBranch());
        assertEquals("New York", store.getCity());
    }

    @Test
    public void testStoreAllArgsConstructor() {
        Store store = new Store(2, "Mall Store", "Los Angeles");

        assertEquals(Integer.valueOf(2), store.getId());
        assertEquals("Mall Store", store.getBranch());
        assertEquals("Los Angeles", store.getCity());
    }

    @Test
    public void testStoreNoArgsConstructor() {
        Store store = new Store();
        assertNotNull(store);
        assertNull(store.getId());
        assertNull(store.getBranch());
        assertNull(store.getCity());
    }

    @Test
    public void testStoreWithSpecialCharacters() {
        Store store = new Store();
        store.setBranch("Store & Co.");
        store.setCity("Saint-Étienne");

        assertEquals("Store & Co.", store.getBranch());
        assertEquals("Saint-Étienne", store.getCity());
    }

    @Test
    public void testStoreEquality() {
        Store store1 = new Store(1, "Branch1", "City1");
        Store store2 = new Store(1, "Branch1", "City1");

        assertEquals(store1, store2);
        assertEquals(store1.hashCode(), store2.hashCode());
    }

    @Test
    public void testStoreToString() {
        Store store = new Store(1, "Test Branch", "Test City");
        String toString = store.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("Test Branch"));
        assertTrue(toString.contains("Test City"));
    }
}