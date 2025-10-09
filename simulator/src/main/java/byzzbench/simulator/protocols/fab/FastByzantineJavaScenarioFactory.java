package byzzbench.simulator.protocols.fab;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.ScenarioFactory;
import byzzbench.simulator.config.ByzzBenchConfig;
import byzzbench.simulator.domain.Schedule;
import byzzbench.simulator.service.SchedulerService;
import org.springframework.stereotype.Component;

@Component
public class FastByzantineJavaScenarioFactory extends ScenarioFactory {
    public FastByzantineJavaScenarioFactory(SchedulerService schedulerService, ByzzBenchConfig byzzBenchConfig) {
        super(schedulerService, byzzBenchConfig);
    }

    @Override
    public String getId() {
        return "fab-java";
    }

    @Override
    public Scenario createScenario(Schedule schedule) {
        FastByzantineScenario scenarioExecutor = new FastByzantineScenario(schedule);
        scenarioExecutor.loadParameters(schedule.getParameters());
        return scenarioExecutor;
    }
}
