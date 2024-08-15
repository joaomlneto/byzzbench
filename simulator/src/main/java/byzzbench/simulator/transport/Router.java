package byzzbench.simulator.transport;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;

/**
 * A router that manages partitions of nodes.
 * Nodes on different partitions cannot communicate with each other.
 * Nodes without partition IDs are in partition 0 and can communicate with each
 * other.
 */
public class Router {
  public static final int DEFAULT_PARTITION = 0;

  /**
   * The sequence number for events.
   */
  private final AtomicInteger partitionSequenceNumber = new AtomicInteger(1);

  /**
   * Map of Node ID to the partition ID that the node is in.
   * Two nodes on different partitions cannot communicate.
   * Nodes without partition IDs are in partition 0 and can
   * communicate with each other.
   */
  @Getter private final Map<String, Integer> partitions = new HashMap<>();

  /**
   * Isolates a node from the rest of the network.
   * @param nodeId The ID of the node to isolate.
   */
  public void isolateNode(String nodeId) {
    this.isolateNodes(new String[] {nodeId});
  }

  /**
   * Isolates a set of nodes from the rest of the network, but not from each
   * other.
   * @param nodeIds The IDs of the nodes to isolate.
   */
  public void isolateNodes(String[] nodeIds) {
    int newPartition = getUnusedPartitionId();
    for (String nodeId : nodeIds) {
      this.partitions.put(nodeId, newPartition);
    }
  }

  /**
   * Re-joins a node to the rest of the network.
   * @param nodeId The ID of the node to re-join.
   */
  public void healNode(String nodeId) { this.partitions.remove(nodeId); }

  /**
   * Resets the router to its initial state: no partitions.
   */
  public void resetPartitions() {
    this.partitions.clear();
    this.partitionSequenceNumber.set(1);
  }

  /**
   * Gets the partition ID of a node.
   * @param nodeId The ID of the node.
   * @return The partition ID of the node.
   */
  public int getNodePartition(String nodeId) {
    return this.partitions.getOrDefault(nodeId, DEFAULT_PARTITION);
  }

  /**
   * Checks if two nodes are on the same partition.
   * @param nodeId1 The ID of the first node.
   * @param nodeId2 The ID of the second node.
   * @return True if the nodes are on the same partition, false otherwise.
   */
  public boolean haveConnectivity(String nodeId1, String nodeId2) {
    return this.getNodePartition(nodeId1) == this.getNodePartition(nodeId2);
  }

  /**
   * Checks if there are any active partitions.
   * @return True if there are active partitions, false otherwise.
   */
  public boolean hasActivePartitions() { return !this.partitions.isEmpty(); }

  /**
   * Gets the next unused partition ID.
   * @return The next unused partition ID.
   */
  private int getUnusedPartitionId() {
    return partitionSequenceNumber.getAndIncrement();
  }
}
