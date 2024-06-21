package byzzbench.simulator.faults.triggers;

import byzzbench.simulator.faults.FaultTrigger;
import byzzbench.simulator.transport.MessageEvent;
import byzzbench.simulator.transport.Transport;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * Fault trigger that checks if the sender and recipient of a message are in the same network partition.
 * If they are not, the trigger is activated.
 *
 * @param <T>
 */
@RequiredArgsConstructor
public class NodesNotInSameNetworkPartitionFaultTrigger<T extends Serializable> implements FaultTrigger {
    private final Transport<T> transport;

    @Override
    public boolean isTriggeredBy(MessageEvent message) {
        String sender = message.getSenderId();
        String recipient = message.getRecipientId();
        Map<String, Integer> partitions = transport.getPartitions();
        boolean nodesInSamePartition = partitions.getOrDefault(sender, 0).equals(partitions.getOrDefault(recipient, 0));
        return !nodesInSamePartition;
    }
}
