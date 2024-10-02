package byzzbench.simulator.protocols.XRPL;

import byzzbench.simulator.BaseScenarioFactory;
import byzzbench.simulator.Scenario;
import byzzbench.simulator.scheduler.Scheduler;
import byzzbench.simulator.service.MessageMutatorService;
import byzzbench.simulator.service.SchedulerFactoryService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

@Component
public class XRPLScenarioFactory extends BaseScenarioFactory {
  public XRPLScenarioFactory(SchedulerFactoryService schedulerFactoryService) {
    super(schedulerFactoryService);
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
