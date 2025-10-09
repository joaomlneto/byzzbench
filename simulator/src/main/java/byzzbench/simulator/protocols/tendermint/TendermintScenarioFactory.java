package byzzbench.simulator.protocols.tendermint;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.ScenarioFactory;
import byzzbench.simulator.config.ByzzBenchConfig;
import byzzbench.simulator.domain.Schedule;
import byzzbench.simulator.service.SchedulerService;
import org.springframework.stereotype.Component;

@Component
public class TendermintScenarioFactory extends ScenarioFactory {
    public TendermintScenarioFactory(SchedulerService schedulerService, ByzzBenchConfig byzzBenchConfig) {
        super(schedulerService, byzzBenchConfig);
    }

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
