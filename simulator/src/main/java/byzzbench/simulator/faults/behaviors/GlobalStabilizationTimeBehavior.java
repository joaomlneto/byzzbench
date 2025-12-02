package byzzbench.simulator.faults.behaviors;

import byzzbench.simulator.domain.Action;
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
    public Action toAction(ScenarioContext context) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Deprecated
    public void accept(ScenarioContext ctx) {
        ctx.getScenario().getTransport().globalStabilizationTime();
    }
}
