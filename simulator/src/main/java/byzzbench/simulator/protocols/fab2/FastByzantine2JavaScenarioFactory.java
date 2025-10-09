package byzzbench.simulator.protocols.fab2;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.ScenarioFactory;
import byzzbench.simulator.config.ByzzBenchConfig;
import byzzbench.simulator.domain.Schedule;
import byzzbench.simulator.service.SchedulerService;
import org.springframework.stereotype.Component;

@Component
public class FastByzantine2JavaScenarioFactory extends ScenarioFactory {
    public FastByzantine2JavaScenarioFactory(SchedulerService schedulerService, ByzzBenchConfig byzzBenchConfig) {
        super(schedulerService, byzzBenchConfig);
    }

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
