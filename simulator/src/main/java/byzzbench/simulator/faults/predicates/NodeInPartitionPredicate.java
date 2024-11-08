package byzzbench.simulator.faults.predicates;

import byzzbench.simulator.faults.FaultContext;
import byzzbench.simulator.faults.FaultPredicate;
import byzzbench.simulator.transport.Router;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NodeInPartitionPredicate implements FaultPredicate {
  private final String nodeId;
  private final int partitionId;

  @Override
  public String getId() {
    return "NodeInPartitionPredicate-%d".formatted(this.nodeId);
  }

  @Override
  public String getName() {
    return "Node %s in partition %d".formatted(this.nodeId, this.partitionId);
  }

  @Override
  public boolean test(FaultContext ctx) {
    Router router = ctx.getScenario().getTransport().getRouter();
    return router.getNodePartition(nodeId) == partitionId;
  }
}
