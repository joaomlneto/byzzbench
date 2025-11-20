package byzzbench.simulator.state;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.ScenarioPredicate;
import byzzbench.simulator.transport.Event;
import lombok.extern.java.Log;

/**
 * Predicate that checks if the scenario satisfies the liveness property.
 */
@Log
public class DeadlockPredicate extends ScenarioPredicate {
    public DeadlockPredicate(Scenario scenario) {
        super(scenario);
    }

    @Override
    public String getId() {
        return "Deadlock";
    }

    @Override
    public boolean test(Scenario scenarioExecutor) {
        boolean hasQueuedEvents = !(scenarioExecutor.getTransport().getEventsInState(Event.Status.QUEUED).isEmpty());
        if (!hasQueuedEvents) {
            log.info("LivenessPredicate: No events in the QUEUED state");
        }
        return hasQueuedEvents;
    }
}
