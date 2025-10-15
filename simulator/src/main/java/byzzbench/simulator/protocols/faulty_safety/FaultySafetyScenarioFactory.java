package byzzbench.simulator.protocols.faulty_safety;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.ScenarioFactory;
import byzzbench.simulator.domain.Schedule;
import org.springframework.stereotype.Component;

@Component
public class FaultySafetyScenarioFactory extends ScenarioFactory {

    @Override
    public String getId() {
        return "faulty-safety";
    }

    @Override
    public Scenario createScenario(Schedule schedule) {
        FaultySafetyScenario scenarioExecutor = new FaultySafetyScenario(schedule);
        scenarioExecutor.loadParameters(schedule.getParameters());
        return scenarioExecutor;
    }
}
