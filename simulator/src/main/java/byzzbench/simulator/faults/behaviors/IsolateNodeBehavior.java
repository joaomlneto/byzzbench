package byzzbench.simulator.faults.behaviors;

import byzzbench.simulator.faults.FaultBehavior;
import byzzbench.simulator.faults.FaultContext;
import byzzbench.simulator.transport.Router;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class IsolateNodeBehavior implements FaultBehavior {
  private final String nodeId;

  @Override
  public String getId() {
    return "isolatenode-%s".formatted(this.nodeId);
  }

  @Override
  public String getName() {
    return "Isolate node %s".formatted(this.nodeId);
  }

  @Override
  public void accept(FaultContext context) {
    Router router = context.getScenario().getTransport().getRouter();
    router.isolateNode(nodeId);
  }
}
