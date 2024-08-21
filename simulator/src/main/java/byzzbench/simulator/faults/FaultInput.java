package byzzbench.simulator.faults;

import byzzbench.simulator.ScenarioExecutor;
import byzzbench.simulator.transport.Event;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.Optional;

public class FaultInput {
    @Getter
    private final ScenarioExecutor scenario;
    private final Event eventOptional;

    public FaultInput(@NotNull ScenarioExecutor scenario, @NotNull Event event) {
        this.scenario = scenario;
        this.eventOptional = event;
    }

    public FaultInput(ScenarioExecutor scenario) {
        this.scenario = scenario;
        this.eventOptional = null;
    }

    public Optional<Event> getEvent() {
        return Optional.ofNullable(eventOptional);
    }
}
