package byzzbench.simulator.state;

import byzzbench.simulator.ScenarioExecutor;
import byzzbench.simulator.transport.Event;

import java.io.Serializable;
import java.util.function.Predicate;

/**
 * Predicate that checks if the scenario satisfies the liveness property.
 * @param <T> The type of the entries in the commit log of each {@link byzzbench.simulator.Replica}.
 */
public class LivenessPredicate<T extends Serializable> implements Predicate<ScenarioExecutor<T>> {
    @Override
    public boolean test(ScenarioExecutor<T> scenarioExecutor) {
        boolean hasNoQueuedEvents = scenarioExecutor.getTransport().getEventsInState(Event.Status.QUEUED).isEmpty();
        if (hasNoQueuedEvents) {
            System.out.println("LivenessPredicate: No events in the QUEUED state");
        }
        return !hasNoQueuedEvents;
    }
}
