package byzzbench.simulator;

import byzzbench.simulator.domain.Schedule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public abstract class ScenarioFactory {
    public String getId() {
        return this.getClass().getSimpleName();
    }

    public abstract Scenario createScenario(Schedule schedule);
}
