package byzzbench.simulator.faults.predicates;

import byzzbench.simulator.faults.FaultPredicate;
import byzzbench.simulator.faults.ScenarioContext;

/**
 * A predicate that always evaluates to true
 */
public class TruePredicate implements FaultPredicate {
    @Override
    public String getId() {
        return "true";
    }

    @Override
    public String getName() {
        return "true";
    }

    @Override
    public boolean test(ScenarioContext scenarioContext) {
        return true;
    }
}
