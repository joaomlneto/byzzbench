package byzzbench.simulator.faults;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.utils.NonNull;
import java.util.Optional;
import lombok.Getter;

public class FaultContext {
  @Getter @NonNull private final Scenario scenario;

  private final Event eventOptional;

  public FaultContext(@NonNull Scenario scenario, @NonNull Event event) {
    this.scenario = scenario;
    this.eventOptional = event;
  }

  public FaultContext(@NonNull Scenario scenario) {
    this.scenario = scenario;
    this.eventOptional = null;
  }

  public Optional<Event> getEvent() {
    return Optional.ofNullable(eventOptional);
  }

  public boolean hasEvent() { return eventOptional != null; }
}
