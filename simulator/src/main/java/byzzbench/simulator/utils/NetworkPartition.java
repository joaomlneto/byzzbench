package byzzbench.simulator.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to represent a network partition.
 * TODO: add checks for partitions that are not disjoint.
 */
public class NetworkPartition {
    /**
     * The list of partitions.
     * Each partition is a list of node IDs.
     */
    List<List<String>> partitions = new ArrayList<>();

    /**
     * Create a new network partition with the given partitions.
     *
     * @param partitions the partitions
     */
    public NetworkPartition(List<List<String>> partitions) {
        this.partitions = new ArrayList<>(partitions);
    }

    /**
     * Create a new network partition with no partitions.
     */
    public NetworkPartition() {
        this(List.of());
    }

    /**
     * Checks if two nodes can communicate.
     *
     * @param from the sender
     * @param to   the receiver
     */
    public boolean canCommunicate(String from, String to) {
        for (List<String> partition : partitions) {
            if (partition.contains(from) && partition.contains(to)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Add a partition to the network partition.
     *
     * @param partition the partition to add
     */
    public void addPartition(List<String> partition) {
        partitions.add(partition);
    }

    /**
     * Add partitions to the network partition.
     *
     * @param partitions the partitions to add
     */
    public void addPartitions(List<List<String>> partitions) {
        this.partitions.addAll(partitions);
    }
}
