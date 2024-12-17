package byzzbench.simulator.utils;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.*;

/**
 * Utility class to compute Stirling numbers of the second kind.
 */
public class StirlingNumberSecondKind {
    /**
     * Memoization map to store computed Stirling numbers.
     */
    private static final Map<MemoEntryKey, Long> memo = new HashMap<>();

    /**
     * Random number generator.
     */
    private static final Random rand = new Random();

    private StirlingNumberSecondKind() {
        // Utility class - cannot be instantiated
    }

    /**
     * Compute the Stirling number of the second kind.
     *
     * @param n total number of elements
     * @param k number of non-empty subsets
     * @return the Stirling number of the second kind S(n, k)
     */
    public static long stirlingNumber(int n, int k) {
        if (n == 0 && k == 0) return 1;
        if (n == 0 || k == 0) return 0;
        if (k == 1) return 1;
        if (k == n) return 1;

        MemoEntryKey key = new MemoEntryKey(n, k);
        if (memo.containsKey(key)) {
            return memo.get(key);
        }

        long result = k * stirlingNumber(n - 1, k) + stirlingNumber(n - 1, k - 1);
        memo.put(key, result);
        return result;
    }

    /**
     * Get the partition associated with the nth Stirling number of the second kind.
     *
     * @param elements the elements to partition
     * @param k        the number of non-empty subsets
     * @param i        the i-th partition to get
     * @param <T>      the type of the elements
     * @return k partitions of the elements
     */
    public static <T> List<List<T>> getIthPartition(Collection<T> elements, int k, int i) {
        List<List<List<T>>> partitions = getPartitions(new ArrayList<>(elements), k);
        return partitions.get(i % partitions.size());
    }

    /**
     * Generate all partitions of the elements into k non-empty subsets.
     *
     * @param elements the elements to partition
     * @param k        the number of non-empty subsets
     * @param <T>      the type of the elements
     * @return k partitions of the elements
     */
    public static <T> List<List<List<T>>> getPartitions(List<T> elements, int k) {
        // initialize result
        List<List<List<T>>> result = new ArrayList<>();

        // initialize k partitions
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            partitions.add(new ArrayList<>());
        }

        // compute partitions
        getPartitions(result, partitions, elements, k, k, elements.size(), 0, -1);

        return result;
    }

    /**
     * Recursive helper function to compute partitions.
     */
    private static <T> void getPartitions(
            List<List<List<T>>> result,
            List<List<T>> parts,
            List<T> elements,
            int k,
            int empty,
            int n,
            int m,
            int lastfilled) {
        if (m == n) {
            //System.out.println("m == n: " + parts);
            List<List<T>> partition = new ArrayList<>();
            for (List<T> part : parts) {
                partition.add(new ArrayList<>(part));
            }
            result.add(partition);
            return;
        }
        int start = (n - m == empty) ? (k - empty) : 0;

        for (int i = start; i < Math.min(k, lastfilled + 2); i++) {
            parts.get(i).add(elements.get(m));
            if (parts.get(i).size() == 1) {
                empty -= 1;
            }
            getPartitions(result, parts, elements, k, empty, n, m + 1, Math.max(i, lastfilled));
            parts.get(i).removeLast();
            if (parts.get(i).isEmpty()) {
                empty += 1;
            }
        }
    }

    /**
     * Get a random partition of the elements into k non-empty subsets.
     *
     * @param elements the collection of elements to partition
     * @param k        the number of non-empty subsets to partition into
     * @param <T>      the type of the elements
     * @return k partitions of the elements
     */
    public static <T> Collection<Collection<T>> getRandomPartition(Collection<T> elements, int k) {
        List<Collection<T>> partitions = new ArrayList<>();
        List<T> sortedElements = new ArrayList<>(elements);

        // add k entries to partitions
        for (int i = 0; i < k; i++) {
            partitions.add(new ArrayList<>());
        }

        // make sure each partition is non-empty
        for (int i = 0; i < k; i++) {
            // get random element
            int index = rand.nextInt(sortedElements.size());
            T el = sortedElements.remove(index);
            // insert into partition i
            partitions.get(i).add(el);
        }

        // distribute the rest of the elements
        for (T el : sortedElements) {
            partitions.get(rand.nextInt(k)).add(el);
        }

        // return the partitions
        return partitions;
    }

    @Data
    @EqualsAndHashCode
    public static class MemoEntryKey {
        private final long n;
        private final long k;
    }
}
