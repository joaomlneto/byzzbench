package byzzbench.simulator.state;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.ScenarioPredicate;
import byzzbench.simulator.transport.Action;

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
        // If we are before GST, the scenario is considered live
        if (!scenarioExecutor.getTransport().isGlobalStabilizationTime()) {
            return true;
        }

        boolean hasQueuedEvents = !(scenarioExecutor.getTransport().getEventsInState(Action.Status.QUEUED).isEmpty());
        if (!hasQueuedEvents) {
            System.out.println("LivenessPredicate: No events in the QUEUED state");
        }
        return hasQueuedEvents;
    }
}
