package byzzbench.simulator.protocols.pbft_java;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.ScenarioPredicate;

public class PbftTerminationPredicate extends ScenarioPredicate {
    public PbftTerminationPredicate(Scenario scenario) {
        super(scenario);
    }

    @Override
    public boolean test(Scenario scenario) {
        return scenario.getSchedule().getLength() > 100;
    }
}
