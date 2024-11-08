package byzzbench.simulator.protocols.XRPL;

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
public class XRPLScenarioFactory extends BaseScenarioFactory {
  public XRPLScenarioFactory(SchedulerFactoryService schedulerFactoryService,
                             ByzzBenchConfig byzzBenchConfig,
                             ObjectMapper objectMapper) {
    super(schedulerFactoryService, byzzBenchConfig, objectMapper);
  }

  @Override
  public String getId() {
    return "xrpl";
  }

  @Override
  public Scenario createScenario(MessageMutatorService messageMutatorService,
                                 JsonNode params) {
    Scheduler scheduler = this.createScheduler(messageMutatorService, params);
    XRPLScenario scenarioExecutor = new XRPLScenario(scheduler);
    scenarioExecutor.loadParameters(params);
    return scenarioExecutor;
  }
}
