package byzzbench.simulator.faults.predicates;

import byzzbench.simulator.faults.FaultContext;
import byzzbench.simulator.faults.FaultPredicate;

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
    public boolean test(FaultContext faultContext) {
        return !faultContext.getScenario().getTransport().isGlobalStabilizationTime();
    }
}
