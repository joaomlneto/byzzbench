package byzzbench.simulator;

import byzzbench.simulator.scheduler.RandomScheduler;
import byzzbench.simulator.scheduler.Scheduler;
import byzzbench.simulator.service.MessageMutatorService;
import byzzbench.simulator.service.SchedulerFactoryService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class BaseScenarioFactory implements ScenarioFactory {
    @Getter(AccessLevel.PROTECTED)
    private final SchedulerFactoryService schedulerFactoryService;

    public Scheduler createScheduler(MessageMutatorService messageMutatorService, JsonNode params) {
        Scheduler scheduler = null;
        if (params.has("scheduler")) {
            scheduler = schedulerFactoryService.getScheduler(params.get("scheduler").get("id").asText(), params.get("scheduler"));
            scheduler.loadParameters(params.get("scheduler"));
        } else {
            scheduler = new RandomScheduler(messageMutatorService);
        }
        return scheduler;
    }
}
