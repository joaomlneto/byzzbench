package byzzbench.simulator;

import byzzbench.simulator.config.ByzzBenchConfig;
import byzzbench.simulator.domain.Schedule;
import byzzbench.simulator.service.SchedulerService;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public abstract class ScenarioFactory {
    @Getter(AccessLevel.PROTECTED)
    private final SchedulerService schedulerService;
    @Getter
    private final ByzzBenchConfig byzzBenchConfig;

    public String getId() {
        return this.getClass().getSimpleName();
    }

    public abstract Scenario createScenario(Schedule schedule);
}
