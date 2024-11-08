package byzzbench.simulator.protocols.pbft;

import byzzbench.simulator.BaseScenarioFactory;
import byzzbench.simulator.Scenario;
import byzzbench.simulator.config.ByzzBenchConfig;
import byzzbench.simulator.scheduler.Scheduler;
import byzzbench.simulator.service.MessageMutatorService;
import byzzbench.simulator.service.SchedulerFactoryService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

//@Component
public class PbftScenarioFactory extends BaseScenarioFactory {
  public PbftScenarioFactory(SchedulerFactoryService schedulerFactoryService,
                             ByzzBenchConfig byzzBenchConfig,
                             ObjectMapper objectMapper) {
    super(schedulerFactoryService, byzzBenchConfig, objectMapper);
  }

  @Override
  public String getId() {
    return "pbft";
  }

  @Override
  public Scenario createScenario(MessageMutatorService messageMutatorService,
                                 JsonNode params) {
    Scheduler scheduler = this.createScheduler(messageMutatorService, params);
    Scenario scenarioExecutor = new PbftScenario(scheduler);
    scenarioExecutor.loadParameters(params);
    return scenarioExecutor;
  }
}
