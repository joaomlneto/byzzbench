package byzzbench.simulator.state;

import byzzbench.simulator.ScenarioExecutor;
import byzzbench.simulator.transport.Event;
import java.util.function.Predicate;

/**
 * Predicate that checks if the scenario satisfies the liveness property.
 */
public class LivenessPredicate implements Predicate<ScenarioExecutor> {
  @Override
  public boolean test(ScenarioExecutor scenarioExecutor) {
    boolean hasNoQueuedEvents = scenarioExecutor.getTransport()
                                    .getEventsInState(Event.Status.QUEUED)
                                    .isEmpty();
    if (hasNoQueuedEvents) {
      System.out.println("LivenessPredicate: No events in the QUEUED state");
    }
    return !hasNoQueuedEvents;
  }
}
