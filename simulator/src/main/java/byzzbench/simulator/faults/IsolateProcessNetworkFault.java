package byzzbench.simulator.faults;

import byzzbench.simulator.transport.Router;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * A fault that isolates a given node from the network
 */
@Getter
@ToString
@RequiredArgsConstructor
public class IsolateProcessNetworkFault implements Fault {
  /**
   * The ID of the node to isolate
   */
  @NonNull private final String nodeId;

  public String getId() {
    return "IsolateProcessNetworkFault(%s)".formatted(this.nodeId);
  }

  public String getName() { return "Isolate %s".formatted(this.nodeId); }

  /**
   * Checks if the node is not already isolated
   * @param ctx The context of the fault
   * @return True if the node is not already isolated, false otherwise
   */
  @Override
  public final boolean test(FaultInput ctx) {
    Router router = ctx.getScenario().getTransport().getRouter();
    return router.getNodePartition(nodeId) == Router.DEFAULT_PARTITION;
  }

  /**
   * Isolates the node from the network
   * @param ctx The context of the fault
   */
  @Override
  public void accept(FaultInput ctx) {
    Router router = ctx.getScenario().getTransport().getRouter();
    router.isolateNode(nodeId);
  }
}
