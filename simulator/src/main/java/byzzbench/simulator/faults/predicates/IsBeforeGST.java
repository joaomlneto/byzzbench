package byzzbench.simulator.faults.predicates;

import byzzbench.simulator.faults.FaultPredicate;
import byzzbench.simulator.faults.ScenarioContext;

public class IsBeforeGST implements FaultPredicate {
    @Override
    public String getId() {
        return "IsBeforeGST";
    }

    @Override
    public String getName() {
        return "Is before GST";
    }

    @Override
    public boolean test(ScenarioContext scenarioContext) {
        return !scenarioContext.getScenario().getTransport().isGlobalStabilizationTime();
    }
}
