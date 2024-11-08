package byzzbench.simulator.protocols.pbft_java;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.ScenarioPredicate;

public class PbftTerminationPredicate implements ScenarioPredicate {
  @Override
  public boolean test(Scenario scenario) {
    return scenario.getSchedule().getEvents().size() > 100;
  }
}
