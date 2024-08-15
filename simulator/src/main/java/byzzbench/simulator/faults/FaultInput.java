package byzzbench.simulator.faults;

import byzzbench.simulator.ScenarioExecutor;
import byzzbench.simulator.transport.Event;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Optional;
import lombok.Getter;

public class FaultInput<T extends Serializable> {
  @Getter private final ScenarioExecutor<T> scenario;
  private final Event eventOptional;

  public FaultInput(@NotNull ScenarioExecutor<T> scenario,
                    @NotNull Event event) {
    this.scenario = scenario;
    this.eventOptional = event;
  }

  public FaultInput(ScenarioExecutor<T> scenario) {
    this.scenario = scenario;
    this.eventOptional = null;
  }

  public Optional<Event> getEvent() {
    return Optional.ofNullable(eventOptional);
  }
}
