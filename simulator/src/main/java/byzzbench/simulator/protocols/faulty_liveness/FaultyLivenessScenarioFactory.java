package byzzbench.simulator.protocols.faulty_liveness;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.ScenarioFactory;
import byzzbench.simulator.domain.Schedule;
import org.springframework.stereotype.Component;

@Component
public class FaultyLivenessScenarioFactory extends ScenarioFactory {

    @Override
    public String getId() {
        return "faulty-liveness";
    }

    @Override
    public Scenario createScenario(Schedule schedule) {
        FaultyLivenessScenario scenarioExecutor = new FaultyLivenessScenario(schedule);
        scenarioExecutor.loadParameters(schedule.getParameters());
        return scenarioExecutor;
    }
}
