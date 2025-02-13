package com.perfecthash;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;

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

    @Test
    public void benchmarkTest() {
        // Test parameters
        final int[] dataSizes = {1000, 10000, 100000};
        final int numLookups = 1000000;
        
        System.out.println("\nBenchmark Results:");
        System.out.println("=================");
        
        for (int size : dataSizes) {
            // Generate test data
            Set<String> keys = new HashSet<>();
            for (int i = 0; i < size; i++) {
                keys.add("key" + i);
            }
            
            // Benchmark HashMap initialization
            long startTime = System.nanoTime();
            Map<String, Integer> hashMap = new HashMap<>();
            for (String key : keys) {
                hashMap.put(key, key.hashCode());
            }
            long hashMapInitTime = System.nanoTime() - startTime;
            
            // Benchmark PerfectHashMap initialization
            startTime = System.nanoTime();
            PerfectHashMap<String, Integer> perfectHashMap = new PerfectHashMap<>(keys);
            for (String key : keys) {
                perfectHashMap.put(key, key.hashCode());
            }
            long perfectHashMapInitTime = System.nanoTime() - startTime;
            
            // Prepare random keys for lookup (75% existing, 25% non-existing)
            List<String> lookupKeys = new ArrayList<>();
            Random random = new Random(42); // Fixed seed for reproducibility
            for (int i = 0; i < numLookups; i++) {
                if (random.nextDouble() < 0.75) {
                    // Existing key
                    lookupKeys.add("key" + random.nextInt(size));
                } else {
                    // Non-existing key
                    lookupKeys.add("nonexistent" + random.nextInt(size));
                }
            }
            
            // Benchmark HashMap lookup
            startTime = System.nanoTime();
            int hashMapHits = 0;
            for (String key : lookupKeys) {
                if (hashMap.get(key) != null) {
                    hashMapHits++;
                }
            }
            long hashMapLookupTime = System.nanoTime() - startTime;
            
            // Benchmark PerfectHashMap lookup
            startTime = System.nanoTime();
            int perfectHashMapHits = 0;
            for (String key : lookupKeys) {
                if (perfectHashMap.get(key) != null) {
                    perfectHashMapHits++;
                }
            }
            long perfectHashMapLookupTime = System.nanoTime() - startTime;
            
            // Print results
            System.out.printf("\nData size: %d entries%n", size);
            System.out.println("----------------------------------------");
            System.out.printf("Initialization time:%n");
            System.out.printf("HashMap:        %8.2f ms%n", hashMapInitTime / 1_000_000.0);
            System.out.printf("PerfectHashMap: %8.2f ms%n", perfectHashMapInitTime / 1_000_000.0);
            System.out.printf("%nLookup time (%d operations):%n", numLookups);
            System.out.printf("HashMap:        %8.2f ms (%.2f ns/op)%n", 
                hashMapLookupTime / 1_000_000.0, 
                (double) hashMapLookupTime / numLookups);
            System.out.printf("PerfectHashMap: %8.2f ms (%.2f ns/op)%n", 
                perfectHashMapLookupTime / 1_000_000.0,
                (double) perfectHashMapLookupTime / numLookups);
            System.out.printf("Hits: HashMap=%d, PerfectHashMap=%d%n", hashMapHits, perfectHashMapHits);
        }
    }
}
