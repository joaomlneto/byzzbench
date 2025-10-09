package byzzbench.simulator.protocols.dummy;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.ScenarioFactory;
import byzzbench.simulator.config.ByzzBenchConfig;
import byzzbench.simulator.domain.Schedule;
import byzzbench.simulator.service.SchedulerService;
import org.springframework.stereotype.Component;

@Component
public class DummyScenarioFactory extends ScenarioFactory {
    public DummyScenarioFactory(SchedulerService schedulerService, ByzzBenchConfig byzzBenchConfig) {
        super(schedulerService, byzzBenchConfig);
    }

    @Override
    public String getId() {
        return "dummy";
    }

    @Override
    public Scenario createScenario(Schedule schedule) {
        DummyScenario scenarioExecutor = new DummyScenario(schedule);
        scenarioExecutor.loadParameters(schedule.getParameters());
        return scenarioExecutor;
    }
}
