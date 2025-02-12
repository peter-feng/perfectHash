# Perfect Hash Map Implementation

This library provides a perfect hash map implementation using the FKS (Fredman-Komlós-Szemerédi) scheme. Perfect hashing guarantees O(1) lookup time with no collisions for a static set of keys.

## Features

- O(1) lookup time guaranteed
- No collisions
- Space complexity: O(n²) in worst case, where n is the number of keys
- Static perfect hashing (keys must be known in advance)
- Generic implementation supporting any key type with proper `hashCode()` implementation

## Implementation Details

The implementation uses a two-level hashing scheme:

1. First level: Uses universal hashing to distribute keys into buckets
2. Second level: Each bucket uses a perfect hash function specifically tailored for its keys

## FKS (Fredman-Komlós-Szemerédi) Scheme Explained

The FKS perfect hashing scheme uses a two-level hash function approach to achieve O(1) lookup with no collisions. Here's how it works:

### Algorithm Overview

1. First Level Hash:
   - Maps n keys into m buckets (m ≈ n²)
   - May have collisions, but distributes keys somewhat evenly
   - Uses universal hashing to minimize collisions

2. Second Level Hash:
   - For each bucket, creates a perfect hash function for the keys that mapped to that bucket
   - Each bucket gets its own hash function
   - Guarantees no collisions within each bucket

### Pseudo Code

```
class PerfectHash:
    # First level initialization
    function initialize(keys):
        n = len(keys)
        m = n * n  # Size of first level table
        prime = next_prime(m)  # A prime number larger than m
        
        # Find a good first level hash function
        while true:
            collisions = try_first_level_hash(keys, prime, m)
            if max_bucket_size(collisions) <= 2*n/m:
                break
        
        # Initialize second level tables
        for each bucket in collisions:
            if not empty(bucket):
                create_second_level_table(bucket)
    
    # First level hash function
    function first_level_hash(key):
        return ((a * hash(key)) % prime) % m
        # where 'a' is randomly chosen from [1, prime-1]
    
    # Second level table creation
    function create_second_level_table(keys_in_bucket):
        b = len(keys_in_bucket)
        size = b * b  # Size of second level table
        
        # Find a hash function that creates no collisions
        while true:
            multiplier = random(1, prime-1)
            positions = {}
            collision_found = false
            
            for key in keys_in_bucket:
                pos = second_level_hash(key, multiplier, size)
                if pos in positions:
                    collision_found = true
                    break
                positions[pos] = key
            
            if not collision_found:
                break
        
        return (multiplier, size)
    
    # Second level hash function
    function second_level_hash(key, multiplier, size):
        return ((multiplier * hash(key)) % prime) % size
    
    # Lookup operation
    function get(key):
        bucket = first_level_hash(key)
        if bucket is empty:
            return null
            
        (multiplier, size) = second_level_tables[bucket]
        position = second_level_hash(key, multiplier, size)
        return values[bucket][position]
```

### Space and Time Complexity

- Space Complexity: O(n²) in worst case
  - First level table: O(n²)
  - Second level tables: O(1) per key
  
- Time Complexity:
  - Initialization: O(n) expected
  - Lookup: O(1) worst case
  - Insert: Not supported after initialization
  
### Key Properties

1. Perfect Hashing: No collisions in final hash table
2. Static: Keys must be known in advance
3. Space-Time Tradeoff: Uses more space to achieve O(1) lookup
4. Universal Hashing: Uses randomization to achieve good distribution

## Usage Example

```java
// Create a set of keys
Set<String> keys = new HashSet<>();
keys.add("apple");
keys.add("banana");
keys.add("orange");

// Create the perfect hash map
PerfectHashMap<String, Integer> map = new PerfectHashMap<>(keys);

// Add values
map.put("apple", 1);
map.put("banana", 2);
map.put("orange", 3);

// Retrieve values
int value = map.get("apple"); // Returns 1
```

## Requirements

- Java 11 or higher
- Maven for building

## Building

```bash
mvn clean install
```

## Running Tests

```bash
mvn test
```

## Limitations

1. The key set must be known in advance and cannot be modified after creation
2. Attempting to add keys that were not in the original set will throw an IllegalArgumentException
3. The space complexity is O(n²) in the worst case, though it's usually much better in practice

## License

This project is open source and available under the MIT License.
