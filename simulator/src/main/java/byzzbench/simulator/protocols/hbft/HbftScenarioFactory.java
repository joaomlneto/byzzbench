package byzzbench.simulator.protocols.hbft;

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
public class HbftScenarioFactory extends BaseScenarioFactory {
  public HbftScenarioFactory(SchedulerFactoryService schedulerFactoryService,
                             ByzzBenchConfig byzzBenchConfig,
                             ObjectMapper objectMapper) {
    super(schedulerFactoryService, byzzBenchConfig, objectMapper);
  }

  @Override
  public String getId() {
    return "hbft";
  }

  @Override
  public Scenario createScenario(MessageMutatorService messageMutatorService,
                                 JsonNode params) {
    Scheduler scheduler = this.createScheduler(messageMutatorService, params);
    HbftScenario scenarioExecutor = new HbftScenario(scheduler);
    scenarioExecutor.loadParameters(params);
    return scenarioExecutor;
  }
}
