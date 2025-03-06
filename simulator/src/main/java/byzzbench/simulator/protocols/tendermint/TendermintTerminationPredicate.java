package byzzbench.simulator.protocols.tendermint;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.ScenarioPredicate;

public class TendermintTerminationPredicate implements ScenarioPredicate {
    @Override
    public boolean test(Scenario scenario) {
        return scenario.getSchedule().getActions().size() > 100;
    }
}
