package byzzbench.simulator.faults;

import byzzbench.simulator.ScenarioExecutor;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.utils.NonNull;
import java.util.Optional;
import lombok.Getter;

public class FaultInput {
  @Getter @NonNull private final ScenarioExecutor scenario;

  private final Event eventOptional;

  public FaultInput(@NonNull ScenarioExecutor scenario, @NonNull Event event) {
    this.scenario = scenario;
    this.eventOptional = event;
  }

  public FaultInput(@NonNull ScenarioExecutor scenario) {
    this.scenario = scenario;
    this.eventOptional = null;
  }

  public Optional<Event> getEvent() {
    return Optional.ofNullable(eventOptional);
  }

  public boolean hasEvent() { return eventOptional != null; }
}
