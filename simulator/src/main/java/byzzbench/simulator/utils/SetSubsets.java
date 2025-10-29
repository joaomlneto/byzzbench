package byzzbench.simulator.utils;

import java.util.*;

/**
 * Utility class for generating subsets of a set
 */
public class SetSubsets {
    private SetSubsets() {
        // Utility class - cannot be instantiated
    }

    /**
     * Generate all possible subsets of a set
     *
     * @param set The set to generate subsets for
     * @param <T> The type of the elements in the set
     * @return A collection of all possible subsets of the input set
     */
    public static <T extends Comparable> List<SortedSet<T>> getSubsets(SortedSet<T> set) {
        List<SortedSet<T>> allSubsets = new ArrayList<>();

        int max = 1 << set.size(); // 2^n

        for (int i = 0; i < max; i++) {
            allSubsets.add(getSubset(set, i));
        }

        return allSubsets;
    }

    /**
     * Get a subset of a set based on the index
     *
     * @param set   The set to get a subset of
     * @param index The index of the subset to get
     * @param <T>   The type of the elements in the set
     * @return The subset of the input set
     */
    public static <T extends Comparable> SortedSet<T> getSubset(SortedSet<T> set, int index) {
        SortedSet<T> subset = new TreeSet<>();
        int i = 0;
        for (T element : set) {
            if ((index & (1 << i)) > 0) {
                subset.add(element);
            }
            i++;
        }
        return subset;
    }

    /**
     * Get a random subset of a set, uniformly distributed
     *
     * @param set The set to get a random subset of
     * @param <T> The type of the elements in the set
     * @return A random subset of the input set
     */
    public static <T extends Comparable> SortedSet<T> getRandomSubset(SortedSet<T> set, Random rand) {
        // generate random number between 0 and 2^n
        int random = rand.nextInt(1 << set.size());
        return getSubset(set, random);
    }

    /**
     * Get a random nonempty subset of a set, uniformly distributed
     *
     * @param set The set to get a random subset of
     * @param <T> The type of the elements in the set
     * @return A random subset of the input set
     */
    public static <T extends Comparable> SortedSet<T> getRandomNonEmptySubset(SortedSet<T> set, Random rand) {
        // generate random number between 0 and 2^n
        int random = rand.nextInt(1 << set.size() - 1) + 1;
        return getSubset(set, random);
    }
}
