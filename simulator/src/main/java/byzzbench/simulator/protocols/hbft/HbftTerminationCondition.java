package byzzbench.simulator.protocols.hbft;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.ScenarioPredicate;

public class HbftTerminationCondition implements ScenarioPredicate {
    @Override
    public boolean test(Scenario scenario) {
        return scenario.getSchedule().getEvents().size() > 100;
    }
}
