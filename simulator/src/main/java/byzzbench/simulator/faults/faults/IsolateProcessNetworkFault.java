package byzzbench.simulator.faults.faults;

import byzzbench.simulator.faults.BaseFault;
import byzzbench.simulator.faults.behaviors.IsolateNodeBehavior;
import byzzbench.simulator.faults.predicates.NodeInPartitionPredicate;
import byzzbench.simulator.transport.Router;

/**
 * A fault that isolates a given node from the network, if the node
 * is in the "default" partition.
 * <p>
 * Predicate: Node is in the default partition
 * Behavior: Isolate the node from the network
 */
public class IsolateProcessNetworkFault extends BaseFault {
  public IsolateProcessNetworkFault(String nodeId) {
    super("IsolateProcessNetworkFault-%s".formatted(nodeId),
          new NodeInPartitionPredicate(nodeId, Router.DEFAULT_PARTITION),
          new IsolateNodeBehavior(nodeId));
  }
}
