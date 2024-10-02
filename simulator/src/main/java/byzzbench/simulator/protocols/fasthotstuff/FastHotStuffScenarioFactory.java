package byzzbench.simulator.protocols.fasthotstuff;

import byzzbench.simulator.BaseScenarioFactory;
import byzzbench.simulator.Scenario;
import byzzbench.simulator.scheduler.Scheduler;
import byzzbench.simulator.service.MessageMutatorService;
import byzzbench.simulator.service.SchedulerFactoryService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

@Component
public class FastHotStuffScenarioFactory extends BaseScenarioFactory {
    public FastHotStuffScenarioFactory(SchedulerFactoryService schedulerFactoryService) {
        super(schedulerFactoryService);
    }

    @Override
    public String getId() {
        return "fasthotstuff";
    }

    @Override
    public Scenario createScenario(MessageMutatorService messageMutatorService, JsonNode params) {
        Scheduler scheduler = this.createScheduler(messageMutatorService, params);
        FastHotStuffScenarioExecutor scenarioExecutor = new FastHotStuffScenarioExecutor(scheduler);
        scenarioExecutor.loadParameters(params);
        return scenarioExecutor;
    }
}
