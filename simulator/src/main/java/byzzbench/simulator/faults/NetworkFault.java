package byzzbench.simulator.faults;

import byzzbench.simulator.faults.behaviors.DropMessageFaultBehavior;
import byzzbench.simulator.faults.triggers.NodesNotInSameNetworkPartitionFaultTrigger;
import byzzbench.simulator.transport.Transport;

/**
 * A fault that obey the network partition rule: if the sender and recipient of a message are not in the same network
 * partition, as defined by the routing table of the transport layer, the message is dropped.
 */
public class NetworkFault extends Fault {
    public NetworkFault(Transport<?> transport) {
        super(
                new NodesNotInSameNetworkPartitionFaultTrigger<>(transport),
                new DropMessageFaultBehavior());
    }
}
