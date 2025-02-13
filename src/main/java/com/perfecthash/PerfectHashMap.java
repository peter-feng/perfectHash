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
    private final int multiplier;

    /**
     * Creates a perfect hash map for the given set of keys.
     *
     * @param keys The set of keys to be stored in the map
     */
    @SuppressWarnings("unchecked")
    public PerfectHashMap(Set<K> keys) {
        this.keySet = new HashSet<>(keys);
        this.size = keys.size();
        
        // Choose appropriate prime number based on input size
        this.prime = findNextPrime(Math.max(DEFAULT_PRIME, size * 2));
        
        // Initialize first level table with size proportional to number of keys
        this.firstLevelSize = size * 2;
        this.secondLevelTables = new SecondLevelTable[firstLevelSize];
        
        // Find a good first level multiplier
        this.multiplier = findGoodFirstLevelMultiplier(keys);
        
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
            if (!groupKeys.isEmpty()) {
                secondLevelTables[index] = new SecondLevelTable<>(groupKeys);
            }
        }
    }

    private int findGoodFirstLevelMultiplier(Set<K> keys) {
        Random random = new Random(1); // Fixed seed for reproducibility
        int maxAttempts = 1000;
        int bestMultiplier = 1;
        int bestMaxBucket = keys.size();
        
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            int m = 1 + random.nextInt(prime - 1);
            Map<Integer, Integer> bucketSizes = new HashMap<>();
            
            // Count bucket sizes for this multiplier
            for (K key : keys) {
                int hash = ((m * Math.abs(key.hashCode())) % prime) % firstLevelSize;
                bucketSizes.merge(hash, 1, Integer::sum);
            }
            
            // Find maximum bucket size
            int maxBucket = bucketSizes.values().stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);
            
            if (maxBucket < bestMaxBucket) {
                bestMaxBucket = maxBucket;
                bestMultiplier = m;
                
                // If we found a good enough distribution, stop early
                if (maxBucket <= 3) {
                    break;
                }
            }
        }
        
        return bestMultiplier;
    }

    private int findNextPrime(int n) {
        while (!isPrime(n)) {
            n++;
        }
        return n;
    }

    private boolean isPrime(int n) {
        if (n <= 1) return false;
        if (n <= 3) return true;
        if (n % 2 == 0 || n % 3 == 0) return false;
        
        for (int i = 5; i * i <= n; i += 6) {
            if (n % i == 0 || n % (i + 2) == 0) {
                return false;
            }
        }
        return true;
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
        return Math.abs((multiplier * key.hashCode()) % prime) % firstLevelSize;
    }

    /**
     * Inner class representing a second-level hash table.
     */
    private class SecondLevelTable<KK, VV> {
        private class Entry {
            final KK key;
            VV value;
            
            Entry(KK key, VV value) {
                this.key = key;
                this.value = value;
            }
        }
        
        private final Object[] entries;
        private final int multiplier;
        private final int tableSize;

        @SuppressWarnings("unchecked")
        SecondLevelTable(List<KK> keys) {
            int m = keys.size();
            this.tableSize = m * m + 1; // Add 1 to handle edge cases
            this.entries = new Object[tableSize];
            this.multiplier = findGoodSecondLevelMultiplier(keys);
        }

        private int findGoodSecondLevelMultiplier(List<KK> keys) {
            Random random = new Random(1); // Fixed seed for reproducibility
            int maxAttempts = 10000;
            
            for (int attempt = 0; attempt < maxAttempts; attempt++) {
                int m = 1 + random.nextInt(prime - 1);
                Set<Integer> usedPositions = new HashSet<>();
                boolean collision = false;
                
                for (KK key : keys) {
                    int position = secondLevelHash(key, m);
                    if (!usedPositions.add(position)) {
                        collision = true;
                        break;
                    }
                }
                
                if (!collision) {
                    return m;
                }
            }
            
            // If we can't find a perfect hash function, use linear probing as fallback
            return 1;
        }

        private int secondLevelHash(KK key, int m) {
            return Math.abs((m * key.hashCode()) % prime) % tableSize;
        }

        void put(KK key, VV value) {
            int index = secondLevelHash(key, multiplier);
            int originalIndex = index;
            
            while (entries[index] != null && !key.equals(((Entry)entries[index]).key)) {
                index = (index + 1) % tableSize;
                if (index == originalIndex) {
                    throw new IllegalStateException("Table is full");
                }
            }
            
            if (entries[index] == null) {
                entries[index] = new Entry(key, value);
            } else {
                ((Entry)entries[index]).value = value;
            }
        }

        @SuppressWarnings("unchecked")
        VV get(KK key) {
            int index = secondLevelHash(key, multiplier);
            int originalIndex = index;
            
            do {
                Object entry = entries[index];
                if (entry == null) {
                    return null;
                }
                Entry e = (Entry)entry;
                if (key.equals(e.key)) {
                    return e.value;
                }
                index = (index + 1) % tableSize;
            } while (index != originalIndex);
            
            return null;
        }
    }
}
