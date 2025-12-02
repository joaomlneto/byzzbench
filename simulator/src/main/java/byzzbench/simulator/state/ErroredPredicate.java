package byzzbench.simulator.state;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.ScenarioPredicate;

/**
 * This is a predicate that indicates a scenario has errored.
 * It is only instantiated "manually" by the simulator catching an exception.
 * Its test method should never be called, and it will always throw an exception.
 */
public class ErroredPredicate extends ScenarioPredicate {
    public ErroredPredicate(Scenario scenario) {
        super(scenario);
    }

    @Override
    public String getId() {
        return "Error";
    }

    @Override
    public boolean test(Scenario scenarioExecutor) {
        throw new IllegalStateException("This should never be called!");
    }
}
