package com.perfecthash;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, warmups = 1)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
public class PerfectHashMapBenchmark {

    @Param({"1000", "10000", "100000"})
    private int size;

    @Param({"SEQUENTIAL", "RANDOM", "ZIPFIAN"})
    private String distribution;

    private List<Integer> keys;
    private List<Integer> lookupKeys;
    private Map<Integer, String> hashMap;
    private PerfectHashMap<Integer, String> perfectHashMap;

    private static final Random RANDOM = new Random(42); // Fixed seed for reproducibility

    @Setup(Level.Trial)
    public void setup() {
        // Generate keys based on distribution
        keys = generateKeys();
        // Generate lookup keys (75% hits, 25% misses)
        lookupKeys = generateLookupKeys();
        
        // Initialize HashMap
        hashMap = new HashMap<>();
        for (Integer key : keys) {
            hashMap.put(key, "value-" + key);
        }

        // Initialize PerfectHashMap
        perfectHashMap = new PerfectHashMap<>(new HashSet<>(keys));
        for (Integer key : keys) {
            perfectHashMap.put(key, "value-" + key);
        }
    }

    private List<Integer> generateKeys() {
        switch (distribution) {
            case "SEQUENTIAL":
                return generateSequentialKeys();
            case "RANDOM":
                return generateRandomKeys();
            case "ZIPFIAN":
                return generateZipfianKeys();
            default:
                throw new IllegalArgumentException("Unknown distribution: " + distribution);
        }
    }

    private List<Integer> generateSequentialKeys() {
        return new Random(42).ints(0, Integer.MAX_VALUE)
                .distinct()
                .limit(size)
                .boxed()
                .sorted()
                .collect(Collectors.toList());
    }

    private List<Integer> generateRandomKeys() {
        return new Random(42).ints(0, Integer.MAX_VALUE)
                .distinct()
                .limit(size)
                .boxed()
                .collect(Collectors.toList());
    }

    private List<Integer> generateZipfianKeys() {
        // Generate Zipfian distribution
        double[] probabilities = new double[size];
        double sum = 0;
        for (int i = 1; i <= size; i++) {
            probabilities[i-1] = 1.0 / Math.pow(i, 1.5); // Zipf exponent = 1.5
            sum += probabilities[i-1];
        }
        // Normalize probabilities
        for (int i = 0; i < size; i++) {
            probabilities[i] /= sum;
        }
        
        // Generate keys following Zipfian distribution
        List<Integer> zipfianKeys = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            zipfianKeys.add(i);
        }
        Collections.shuffle(zipfianKeys, new Random(42));
        return zipfianKeys;
    }

    private List<Integer> generateLookupKeys() {
        List<Integer> lookupKeys = new ArrayList<>();
        int numLookups = 1000000;
        int numHits = (int)(numLookups * 0.75); // 75% hits
        
        // Add hits (keys that exist)
        for (int i = 0; i < numHits; i++) {
            lookupKeys.add(keys.get(RANDOM.nextInt(keys.size())));
        }
        
        // Add misses (keys that don't exist)
        Set<Integer> keySet = new HashSet<>(keys);
        for (int i = 0; i < numLookups - numHits; i++) {
            int missKey;
            do {
                missKey = RANDOM.nextInt(Integer.MAX_VALUE);
            } while (keySet.contains(missKey));
            lookupKeys.add(missKey);
        }
        
        Collections.shuffle(lookupKeys, RANDOM);
        return lookupKeys;
    }

    @Benchmark
    public void hashMapLookup(Blackhole blackhole) {
        for (Integer key : lookupKeys) {
            blackhole.consume(hashMap.get(key));
        }
    }

    @Benchmark
    public void perfectHashMapLookup(Blackhole blackhole) {
        for (Integer key : lookupKeys) {
            blackhole.consume(perfectHashMap.get(key));
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(PerfectHashMapBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
