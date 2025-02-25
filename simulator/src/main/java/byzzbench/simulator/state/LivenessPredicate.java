package byzzbench.simulator.state;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.ScenarioPredicate;
import byzzbench.simulator.transport.Event;

/**
 * Predicate that checks if the scenario satisfies the liveness property.
 */
public class LivenessPredicate implements ScenarioPredicate {
    @Override
    public String getId() {
        return "Liveness";
    }

    @Override
    public boolean test(Scenario scenarioExecutor) {
        boolean hasNoQueuedEvents = scenarioExecutor.getTransport().getEventsInState(Event.Status.QUEUED).isEmpty();
        if (hasNoQueuedEvents) {
            System.out.println("LivenessPredicate: No events in the QUEUED state");
        }
        return !hasNoQueuedEvents;
    }
}
