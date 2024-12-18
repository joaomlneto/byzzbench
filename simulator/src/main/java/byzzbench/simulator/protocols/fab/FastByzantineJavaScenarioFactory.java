package byzzbench.simulator.protocols.fab;

import byzzbench.simulator.BaseScenarioFactory;
import byzzbench.simulator.Scenario;
import byzzbench.simulator.config.ByzzBenchConfig;
import byzzbench.simulator.scheduler.Scheduler;
import byzzbench.simulator.service.MessageMutatorService;
import byzzbench.simulator.service.SchedulerFactoryService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class FastByzantineJavaScenarioFactory extends BaseScenarioFactory {
    public FastByzantineJavaScenarioFactory(SchedulerFactoryService schedulerFactoryService, ByzzBenchConfig byzzBenchConfig, ObjectMapper objectMapper) {
        super(schedulerFactoryService, byzzBenchConfig, objectMapper);
    }

    @Override
    public String getId() {
        return "fab-java";
    }

    @Override
    public Scenario createScenario(MessageMutatorService messageMutatorService, JsonNode params) {
        Scheduler scheduler = this.createScheduler(messageMutatorService, params);
        FastByzantineScenario scenarioExecutor = new FastByzantineScenario(scheduler);
        scenarioExecutor.loadParameters(params);
        return scenarioExecutor;
    }
}
