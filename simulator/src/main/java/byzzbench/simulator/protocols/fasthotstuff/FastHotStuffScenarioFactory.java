package byzzbench.simulator.protocols.fasthotstuff;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.ScenarioFactory;
import byzzbench.simulator.domain.Schedule;
import org.springframework.stereotype.Component;

@Component
public class FastHotStuffScenarioFactory extends ScenarioFactory {

    @Override
    public String getId() {
        return "fasthotstuff";
    }

    @Override
    public Scenario createScenario(Schedule schedule) {
        FastHotStuffScenarioExecutor scenarioExecutor = new FastHotStuffScenarioExecutor(schedule);
        scenarioExecutor.loadParameters(schedule.getParameters());
        return scenarioExecutor;
    }
}
