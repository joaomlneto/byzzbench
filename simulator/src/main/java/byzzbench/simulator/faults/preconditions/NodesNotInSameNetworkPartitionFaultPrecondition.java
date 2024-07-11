package byzzbench.simulator.faults.preconditions;

import byzzbench.simulator.faults.FaultPrecondition;
import byzzbench.simulator.transport.MessageEvent;
import byzzbench.simulator.transport.Transport;
import java.io.Serializable;
import java.util.Map;
import lombok.RequiredArgsConstructor;

/**
 * Fault trigger that checks if the sender and recipient of a message are in the
 * same network partition. If they are not, the trigger is activated.
 *
 * @param <T>
 */
@RequiredArgsConstructor
public class NodesNotInSameNetworkPartitionFaultPrecondition<
    T extends Serializable> implements FaultPrecondition {
  private final Transport<T> transport;

  @Override
  public boolean isSatisfiedBy(MessageEvent message) {
    String sender = message.getSenderId();
    String recipient = message.getRecipientId();
    Map<String, Integer> partitions = transport.getPartitions();
    boolean nodesInSamePartition = partitions.getOrDefault(sender, 0).equals(
        partitions.getOrDefault(recipient, 0));
    return !nodesInSamePartition;
  }
}
