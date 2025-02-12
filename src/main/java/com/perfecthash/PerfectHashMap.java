package com.perfecthash;

import java.util.*;

/**
 * A Perfect Hash Map implementation using the FKS (Fredman-Komlós-Szemerédi) scheme.
 * This implementation provides O(1) lookup time with no collisions for a static set of keys.
 * 
 * @param <K> The type of keys
 * @param <V> The type of values
 */
public class PerfectHashMap<K, V> {
    private static final int DEFAULT_PRIME = 7919;
    private final int prime;
    private final SecondLevelTable<K, V>[] secondLevelTables;
    private final int firstLevelSize;
    private final Set<K> keySet;
    private int size;

    /**
     * Creates a perfect hash map for the given set of keys.
     *
     * @param keys The set of keys to be stored in the map
     */
    @SuppressWarnings("unchecked")
    public PerfectHashMap(Set<K> keys) {
        this.prime = DEFAULT_PRIME;
        this.keySet = new HashSet<>(keys);
        this.size = keys.size();
        
        // Initialize first level table
        this.firstLevelSize = size * size;
        this.secondLevelTables = new SecondLevelTable[firstLevelSize];
        
        // Group keys by their first level hash
        Map<Integer, List<K>> firstLevelGroups = new HashMap<>();
        for (K key : keys) {
            int firstHash = firstLevelHash(key);
            firstLevelGroups.computeIfAbsent(firstHash, k -> new ArrayList<>()).add(key);
        }
        
        // Create second level tables
        for (Map.Entry<Integer, List<K>> entry : firstLevelGroups.entrySet()) {
            int index = entry.getKey();
            List<K> groupKeys = entry.getValue();
            secondLevelTables[index] = new SecondLevelTable<>(groupKeys);
        }
    }

    /**
     * Puts a key-value pair into the map.
     *
     * @param key The key
     * @param value The value
     * @throws IllegalArgumentException if the key was not in the original key set
     */
    public void put(K key, V value) {
        if (!keySet.contains(key)) {
            throw new IllegalArgumentException("Key was not in the original key set");
        }
        
        int firstHash = firstLevelHash(key);
        SecondLevelTable<K, V> secondTable = secondLevelTables[firstHash];
        if (secondTable != null) {
            secondTable.put(key, value);
        }
    }

    /**
     * Gets the value associated with the key.
     *
     * @param key The key
     * @return The value associated with the key, or null if not found
     */
    public V get(K key) {
        if (!keySet.contains(key)) {
            return null;
        }
        
        int firstHash = firstLevelHash(key);
        SecondLevelTable<K, V> secondTable = secondLevelTables[firstHash];
        return secondTable != null ? secondTable.get(key) : null;
    }

    /**
     * Returns the number of key-value pairs in the map.
     *
     * @return The size of the map
     */
    public int size() {
        return size;
    }

    private int firstLevelHash(K key) {
        int hash = key.hashCode();
        return Math.abs((hash % prime) % firstLevelSize);
    }

    /**
     * Inner class representing a second-level hash table.
     */
    private class SecondLevelTable<KK, VV> {
        private final VV[] values;
        private final int multiplier;
        private final int tableSize;

        @SuppressWarnings("unchecked")
        SecondLevelTable(List<KK> keys) {
            int m = keys.size();
            this.tableSize = m * m;
            this.values = (VV[]) new Object[tableSize];
            
            // Find a suitable multiplier that creates no collisions
            this.multiplier = findGoodMultiplier(keys);
        }

        private int findGoodMultiplier(List<KK> keys) {
            for (int a = 1; a < prime; a++) {
                if (isGoodMultiplier(a, keys)) {
                    return a;
                }
            }
            throw new IllegalStateException("Could not find a good multiplier");
        }

        private boolean isGoodMultiplier(int a, List<KK> keys) {
            Set<Integer> usedPositions = new HashSet<>();
            for (KK key : keys) {
                int position = secondLevelHash(key, a);
                if (!usedPositions.add(position)) {
                    return false;
                }
            }
            return true;
        }

        private int secondLevelHash(KK key, int a) {
            int hash = key.hashCode();
            return Math.abs(((a * hash) % prime) % tableSize);
        }

        void put(KK key, VV value) {
            int index = secondLevelHash(key, multiplier);
            values[index] = value;
        }

        VV get(KK key) {
            int index = secondLevelHash(key, multiplier);
            return values[index];
        }
    }
}
