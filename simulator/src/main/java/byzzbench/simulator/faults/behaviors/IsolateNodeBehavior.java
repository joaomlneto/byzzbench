package byzzbench.simulator.faults.behaviors;

import byzzbench.simulator.config.FaultBehaviorConfig;
import byzzbench.simulator.domain.FaultInjectionAction;
import byzzbench.simulator.faults.FaultBehavior;
import byzzbench.simulator.faults.ScenarioContext;
import byzzbench.simulator.transport.Router;
import lombok.RequiredArgsConstructor;

import java.util.Map;

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
    public FaultInjectionAction toAction(ScenarioContext context) {
        FaultBehaviorConfig config = FaultBehaviorConfig.builder()
                .faultBehaviorId(getClass().getCanonicalName())
                .params(Map.of("nodeId", this.nodeId))
                .build();
        return FaultInjectionAction.builder()
                .payload(config)
                .build();
    }

    @Deprecated
    public void accept(ScenarioContext context) {
        Router router = context.getScenario().getTransport().getRouter();
        router.isolateNode(nodeId);
    }
}
