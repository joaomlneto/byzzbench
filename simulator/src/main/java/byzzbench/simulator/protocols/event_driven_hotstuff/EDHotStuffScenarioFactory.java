package byzzbench.simulator.protocols.event_driven_hotstuff;

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
public class EDHotStuffScenarioFactory extends BaseScenarioFactory {
    public EDHotStuffScenarioFactory(SchedulerFactoryService schedulerFactoryService, ByzzBenchConfig byzzBenchConfig, ObjectMapper objectMapper) {
        super(schedulerFactoryService, byzzBenchConfig, objectMapper);
    }

    @Override
    public Scenario createScenario(MessageMutatorService messageMutatorService, JsonNode params) {
        Scheduler scheduler = this.createScheduler(messageMutatorService, params);
        EDHotStuffScenario scenario = new EDHotStuffScenario(scheduler);
        scenario.loadScenarioParameters(params);
        return scenario;
    }

    @Override
    public String getId() {
        return "ed-hotstuff";
    }
}
