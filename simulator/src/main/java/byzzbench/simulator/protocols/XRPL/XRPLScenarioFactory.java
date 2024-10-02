package byzzbench.simulator.protocols.XRPL;

import byzzbench.simulator.BaseScenario;
import byzzbench.simulator.ScenarioFactory;
import byzzbench.simulator.scheduler.RandomScheduler;
import byzzbench.simulator.scheduler.Scheduler;
import byzzbench.simulator.service.MessageMutatorService;
import byzzbench.simulator.service.SchedulerFactoryService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class XRPLScenarioFactory implements ScenarioFactory {
  private final SchedulerFactoryService schedulerFactoryService;

  @Override
  public String getId() {
    return "xrpl";
  }

  public Scheduler createScheduler(MessageMutatorService messageMutatorService,
                                   JsonNode params) {
    Scheduler scheduler = null;
    System.out.println("createScheduler() params: " + params);
    if (params.has("scheduler")) {
      scheduler = schedulerFactoryService.getScheduler(
          params.get("scheduler").get("id").asText(), params.get("scheduler"));
      scheduler.loadParameters(params.get("scheduler"));
    } else {
      scheduler = new RandomScheduler(messageMutatorService);
    }
    return scheduler;
  }

  @Override
  public BaseScenario
  createScenario(MessageMutatorService messageMutatorService, JsonNode params) {
    Scheduler scheduler = this.createScheduler(messageMutatorService, params);
    XRPLScenario scenarioExecutor = new XRPLScenario(scheduler);
    scenarioExecutor.loadParameters(params);

    return scenarioExecutor;
  }
}
