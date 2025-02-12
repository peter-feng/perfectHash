package com.perfecthash;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

public class PerfectHashMapTest {

    @Test
    public void testBasicOperations() {
        // Create a set of test keys
        Set<String> keys = new HashSet<>();
        keys.add("apple");
        keys.add("banana");
        keys.add("orange");
        keys.add("grape");
        keys.add("mango");

        // Create perfect hash map
        PerfectHashMap<String, Integer> map = new PerfectHashMap<>(keys);

        // Test putting and getting values
        map.put("apple", 1);
        map.put("banana", 2);
        map.put("orange", 3);
        map.put("grape", 4);
        map.put("mango", 5);

        assertEquals(Integer.valueOf(1), map.get("apple"));
        assertEquals(Integer.valueOf(2), map.get("banana"));
        assertEquals(Integer.valueOf(3), map.get("orange"));
        assertEquals(Integer.valueOf(4), map.get("grape"));
        assertEquals(Integer.valueOf(5), map.get("mango"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPutInvalidKey() {
        Set<String> keys = new HashSet<>();
        keys.add("test1");
        
        PerfectHashMap<String, Integer> map = new PerfectHashMap<>(keys);
        map.put("invalid", 1); // Should throw IllegalArgumentException
    }

    @Test
    public void testGetInvalidKey() {
        Set<String> keys = new HashSet<>();
        keys.add("test1");
        
        PerfectHashMap<String, Integer> map = new PerfectHashMap<>(keys);
        assertNull(map.get("invalid")); // Should return null for invalid key
    }

    @Test
    public void testLargeDataSet() {
        // Create a larger set of test keys
        Set<Integer> keys = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            keys.add(i);
        }

        PerfectHashMap<Integer, String> map = new PerfectHashMap<>(keys);

        // Test putting and getting values
        for (int i = 0; i < 100; i++) {
            map.put(i, "Value" + i);
        }

        // Verify all values
        for (int i = 0; i < 100; i++) {
            assertEquals("Value" + i, map.get(i));
        }
    }
}
