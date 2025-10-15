package byzzbench.simulator.protocols.pbft_java;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.ScenarioFactory;
import byzzbench.simulator.domain.Schedule;
import org.springframework.stereotype.Component;

@Component
public class PbftJavaScenarioFactory extends ScenarioFactory {

    @Override
    public String getId() {
        return "pbft-java";
    }

    @Override
    public Scenario createScenario(Schedule schedule) {
        PbftJavaScenario scenarioExecutor = new PbftJavaScenario(schedule);
        scenarioExecutor.loadParameters(schedule.getParameters());
        return scenarioExecutor;
    }
}
