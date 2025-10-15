package byzzbench.simulator.protocols.hbft;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.ScenarioFactory;
import byzzbench.simulator.domain.Schedule;
import org.springframework.stereotype.Component;

@Component
public class HbftJavaScenarioFactory extends ScenarioFactory {

    @Override
    public String getId() {
        return "hbft";
    }

    @Override
    public Scenario createScenario(Schedule schedule) {
        HbftJavaScenario scenarioExecutor = new HbftJavaScenario(schedule);
        scenarioExecutor.loadParameters(schedule.getParameters());
        return scenarioExecutor;
    }
}
