package byzzbench.simulator.protocols.fab;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.ScenarioPredicate;

public class FastByzantineTerminationCondition implements ScenarioPredicate {
    @Override
    public boolean test(Scenario scenario) {
        return scenario.getSchedule().getActions().size() > 500;
    }
}
