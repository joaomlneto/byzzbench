package byzzbench.simulator.protocols.tendermint;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.ScenarioFactory;
import byzzbench.simulator.domain.Schedule;
import org.springframework.stereotype.Component;

@Component
public class TendermintScenarioFactory extends ScenarioFactory {

    @Override
    public String getId() {
        return "tendermint";
    }

    @Override
    public Scenario createScenario(Schedule schedule) {
        TendermintScenarioExecutor scenarioExecutor = new TendermintScenarioExecutor(schedule);
        scenarioExecutor.loadParameters(schedule.getParameters());
        return scenarioExecutor;
    }
}
