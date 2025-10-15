package byzzbench.simulator.protocols.pbft;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.ScenarioFactory;
import byzzbench.simulator.domain.Schedule;

/**
 * Factory for creating scenarios based on the reference PBFT implementation.
 * <p>
 * FIXME: This is not yet ready and should not be used, hence the
 * commented-out @Component annotation below.
 */
//@Component
public class PbftScenarioFactory extends ScenarioFactory {

    @Override
    public String getId() {
        return "pbft";
    }

    @Override
    public Scenario createScenario(Schedule schedule) {
        Scenario scenarioExecutor = new PbftScenario(schedule);
        scenarioExecutor.loadParameters(schedule.getParameters());
        return scenarioExecutor;
    }
}
