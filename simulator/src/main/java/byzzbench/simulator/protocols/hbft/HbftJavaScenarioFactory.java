package byzzbench.simulator.protocols.hbft;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.ScenarioFactory;
import byzzbench.simulator.config.ByzzBenchConfig;
import byzzbench.simulator.domain.Schedule;
import byzzbench.simulator.service.SchedulerService;
import org.springframework.stereotype.Component;

@Component
public class HbftJavaScenarioFactory extends ScenarioFactory {
    public HbftJavaScenarioFactory(SchedulerService schedulerService, ByzzBenchConfig byzzBenchConfig) {
        super(schedulerService, byzzBenchConfig);
    }

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
