package byzzbench.simulator.state;

import byzzbench.simulator.Replica;
import byzzbench.simulator.Scenario;
import byzzbench.simulator.ScenarioPredicate;
import byzzbench.simulator.protocols.event_driven_hotstuff.EDHotStuffReplica;
import byzzbench.simulator.protocols.event_driven_hotstuff.EDHotStuffScenario;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.TimeoutEvent;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Predicate that checks if the scenario satisfies the liveness property.
 */
public class LivenessPredicate implements ScenarioPredicate {
    private final HashSet<Long> synchronizedViews = new HashSet<>();

    @Override
    public String getId() {
        return "Liveness";
    }

    @Override
    public boolean test(Scenario scenarioExecutor) {
        Collection<EDHotStuffReplica> replicas = scenarioExecutor.getNodes().values().stream()
                .filter(EDHotStuffReplica.class::isInstance)
                .map(EDHotStuffReplica.class::cast)
                .toList();

        //if(replicas.stream().anyMatch(EDHotStuffReplica::isProcessingMessage)) return true;

        boolean hasNoQueuedEvents = scenarioExecutor.getTransport().getEventsInState(Event.Status.QUEUED).stream().filter(e -> !(e instanceof TimeoutEvent)).toList().isEmpty();

        if(scenarioExecutor instanceof EDHotStuffScenario edHotStuffScenario) {
            long currentView = replicas.stream().map(r -> r.getViewNumber()).min(Long::compareTo).get();
            if (replicas.stream().allMatch(r -> r.getViewNumber() == currentView)) synchronizedViews.add(currentView);

            // synchronization view - not necessary for progress to be made
            boolean syncView5 = synchronizedViews.contains(currentView - 5)  && !edHotStuffScenario.hasNonSyncTimeoutForView(currentView - 5) && !edHotStuffScenario.isViewFaulty(currentView - 5);
            boolean commitView5 = edHotStuffScenario.hasCommitForView(currentView - 5);

            // Progress should be made in the next 4 synchronized views
            boolean syncView4 = synchronizedViews.contains(currentView - 4)  && !edHotStuffScenario.hasNonSyncTimeoutForView(currentView - 4) && !edHotStuffScenario.isViewFaulty(currentView - 4);
            boolean commitView4 = edHotStuffScenario.hasCommitForView(currentView - 4);
            boolean syncView3 = synchronizedViews.contains(currentView - 3)  && !edHotStuffScenario.hasNonSyncTimeoutForView(currentView - 3) && !edHotStuffScenario.isViewFaulty(currentView - 3);
            boolean commitView3 = edHotStuffScenario.hasCommitForView(currentView - 3);
            boolean syncView2 = synchronizedViews.contains(currentView - 2)  && !edHotStuffScenario.hasNonSyncTimeoutForView(currentView - 2) && !edHotStuffScenario.isViewFaulty(currentView - 2);
            boolean commitView2 = edHotStuffScenario.hasCommitForView(currentView - 2);
            boolean syncView1 = synchronizedViews.contains(currentView - 1) && !edHotStuffScenario.hasNonSyncTimeoutForView(currentView - 1) && !edHotStuffScenario.isViewFaulty(currentView - 1);
            boolean commitView1 = edHotStuffScenario.hasCommitForView(currentView - 1);

            boolean commitView = edHotStuffScenario.hasCommitForView(currentView);

            boolean noProgress = !commitView && !commitView1 && !commitView2 && !commitView3 && !commitView4 && !commitView5 && syncView1 && syncView2 && syncView3 && syncView4 && syncView5;
            if (noProgress) {
                edHotStuffScenario.log("LivenessPredicate: No progress");
                edHotStuffScenario.log("LivenessPredicate: current view is " + currentView);
                edHotStuffScenario.log("Synchronized views: " + synchronizedViews);
            }
            return !noProgress;
        } else {
            if (hasNoQueuedEvents) System.out.println("LivenessPredicate: No events in the QUEUED state");
            return !hasNoQueuedEvents;
        }
    }
}
