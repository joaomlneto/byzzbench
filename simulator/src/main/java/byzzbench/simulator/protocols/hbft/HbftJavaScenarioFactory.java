package byzzbench.simulator.protocols.hbft;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import byzzbench.simulator.BaseScenarioFactory;
import byzzbench.simulator.Scenario;
import byzzbench.simulator.config.ByzzBenchConfig;
import byzzbench.simulator.scheduler.Scheduler;
import byzzbench.simulator.service.MessageMutatorService;
import byzzbench.simulator.service.SchedulerFactoryService;

@Component
public class HbftJavaScenarioFactory extends BaseScenarioFactory {
    public HbftJavaScenarioFactory(SchedulerFactoryService schedulerFactoryService, ByzzBenchConfig byzzBenchConfig, ObjectMapper objectMapper) {
        super(schedulerFactoryService, byzzBenchConfig, objectMapper);
    }

    @Override
    public String getId() {
        return "hbft";
    }

    @Override
    public Scenario createScenario(MessageMutatorService messageMutatorService, JsonNode params) {
        Scheduler scheduler = this.createScheduler(messageMutatorService, params);
        HbftJavaScenario scenarioExecutor = new HbftJavaScenario(scheduler);
        scenarioExecutor.loadParameters(params);
        return scenarioExecutor;
    }
}
