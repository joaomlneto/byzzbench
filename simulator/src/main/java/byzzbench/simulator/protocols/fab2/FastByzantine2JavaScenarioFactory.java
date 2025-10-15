package byzzbench.simulator.protocols.fab2;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.ScenarioFactory;
import byzzbench.simulator.domain.Schedule;
import org.springframework.stereotype.Component;

@Component
public class FastByzantine2JavaScenarioFactory extends ScenarioFactory {

    @Override
    public String getId() {
        return "fab-java2";
    }

    @Override
    public Scenario createScenario(Schedule schedule) {
        FastByzantineScenario scenarioExecutor = new FastByzantineScenario(schedule);
        scenarioExecutor.loadParameters(schedule.getParameters());
        return scenarioExecutor;
    }
}
