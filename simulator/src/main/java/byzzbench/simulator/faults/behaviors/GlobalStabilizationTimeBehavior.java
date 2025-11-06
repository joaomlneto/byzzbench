package byzzbench.simulator.faults.behaviors;

import byzzbench.simulator.config.FaultBehaviorConfig;
import byzzbench.simulator.domain.FaultInjectionAction;
import byzzbench.simulator.faults.FaultBehavior;
import byzzbench.simulator.faults.ScenarioContext;

public class GlobalStabilizationTimeBehavior implements FaultBehavior {
    @Override
    public String getId() {
        return "GST";
    }

    @Override
    public String getName() {
        return "Global Stabilization Time";
    }

    @Override
    public FaultInjectionAction toAction(ScenarioContext context) {
        FaultBehaviorConfig config = FaultBehaviorConfig.builder().faultBehaviorId(this.getClass().getCanonicalName()).build();
        return FaultInjectionAction.builder().payload(config).build();
    }

    @Deprecated
    public void accept(ScenarioContext ctx) {
        ctx.getScenario().getTransport().globalStabilizationTime();
    }
}
