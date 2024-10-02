package byzzbench.simulator.state;

import byzzbench.simulator.BaseScenario;
import byzzbench.simulator.ScenarioPredicate;
import byzzbench.simulator.transport.Event;

/**
 * Predicate that checks if the scenario satisfies the liveness property.
 */
public class LivenessPredicate implements ScenarioPredicate {
  @Override
  public boolean test(BaseScenario scenarioExecutor) {
    boolean hasNoQueuedEvents = scenarioExecutor.getTransport()
                                    .getEventsInState(Event.Status.QUEUED)
                                    .isEmpty();
    if (hasNoQueuedEvents) {
      System.out.println("LivenessPredicate: No events in the QUEUED state");
    }
    return !hasNoQueuedEvents;
  }
}
