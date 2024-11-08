package byzzbench.simulator;

import byzzbench.simulator.config.ByzzBenchConfig;
import byzzbench.simulator.scheduler.Scheduler;
import byzzbench.simulator.service.MessageMutatorService;
import byzzbench.simulator.service.SchedulerFactoryService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class BaseScenarioFactory implements ScenarioFactory {
  @Getter(AccessLevel.PROTECTED)
  private final SchedulerFactoryService schedulerFactoryService;
  private final ByzzBenchConfig byzzBenchConfig;
  private final ObjectMapper mapper;

  public Scheduler createScheduler(MessageMutatorService messageMutatorService,
                                   JsonNode params) {
    Scheduler scheduler;
    if (params.has("scheduler")) {
      scheduler = schedulerFactoryService.getScheduler(
          params.get("scheduler").get("id").asText(), params.get("scheduler"));
    } else {
      scheduler = schedulerFactoryService.getScheduler(
          byzzBenchConfig.getScheduler().getId(),
          mapper.valueToTree(byzzBenchConfig.getScheduler().getParams())
              .get(1));
    }
    scheduler.loadParameters(params.get("schedulerParams"));
    return scheduler;
  }
}
