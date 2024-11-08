package byzzbench.simulator.scheduler;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.service.MessageMutatorService;
import byzzbench.simulator.transport.ClientRequestEvent;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import byzzbench.simulator.transport.TimeoutEvent;
import byzzbench.simulator.utils.NonNull;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Abstract base class for a scheduler.
 */
@RequiredArgsConstructor
public abstract class BaseScheduler implements Scheduler {
  @NonNull @Getter private final String id;
  @NonNull
  @Getter(AccessLevel.PROTECTED)
  private final transient MessageMutatorService messageMutatorService;
  @Getter protected boolean dropMessages = true;

  /**
   * Loads the parameters for the scheduler from a JSON object.
   *
   * @param parameters The JSON object containing the parameters for the
   *     scheduler.
   */
  public final void loadParameters(JsonNode parameters) {
    // check if drop messages
    if (parameters != null && parameters.has("dropMessages")) {
      this.dropMessages = parameters.get("dropMessages").asBoolean();
    }

    this.loadSchedulerParameters(parameters);
  }

  /**
   * Returns queued events of a specific type in the scenario
   *
   * @param scenario The scenario
   * @return The list of events
   */
  public <T extends Event> List<T> getQueuedEventsOfType(Scenario scenario,
                                                         Class<T> eventClass) {
    return scenario.getTransport()
        .getEventsInState(Event.Status.QUEUED)
        .stream()
        .filter(eventClass::isInstance)
        .map(eventClass::cast)
        .toList();
  }

  /**
   * Returns the queued message events in the scenario
   *
   * @param scenario The scenario
   * @return The list of message events
   */
  public List<MessageEvent> getQueuedMessageEvents(Scenario scenario) {
    return getQueuedEventsOfType(scenario, MessageEvent.class);
  }

  /**
   * Returns the queued timeout events in the scenario
   *
   * @param scenario The scenario
   * @return The list of timeout events
   */
  public List<TimeoutEvent> getQueuedTimeoutEvents(Scenario scenario) {
    return getQueuedEventsOfType(scenario, TimeoutEvent.class);
  }

  /**
   * Returns the queued client request events in the scenario
   *
   * @param scenario The scenario
   * @return the list of client request events
   */
  public List<ClientRequestEvent>
  getQueuedClientRequestEvents(Scenario scenario) {
    return getQueuedEventsOfType(scenario, ClientRequestEvent.class);
  }

  /**
   * Loads the subclass-specific parameters for the scheduler from a JSON
   * object.
   *
   * @param parameters The JSON object containing the parameters for the
   *     scheduler.
   */
  protected abstract void loadSchedulerParameters(JsonNode parameters);
}
