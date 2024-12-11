package byzzbench.simulator.protocols.fab;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.ScenarioPredicate;

public class FabTerminationCondition implements ScenarioPredicate {
    @Override
    public boolean test(Scenario scenario) {
        return scenario.getSchedule().getEvents().size() > 500;
    }
}
