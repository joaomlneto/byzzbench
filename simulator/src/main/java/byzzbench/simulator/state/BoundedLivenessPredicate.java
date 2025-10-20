package byzzbench.simulator.state;

import byzzbench.simulator.*;
import byzzbench.simulator.faults.Fault;
import byzzbench.simulator.service.ApplicationContextProvider;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MutateMessageEventPayload;
import byzzbench.simulator.transport.TimeoutEvent;
import byzzbench.simulator.transport.TransportObserver;
import lombok.extern.java.Log;

import java.io.Serializable;

/**
 * Predicate that checks if the scenario satisfies the liveness property.
 */
@Log
public class BoundedLivenessPredicate extends ScenarioPredicate implements ScenarioObserver, TransportObserver, ReplicaObserver {
    /**
     * The offset index of the GST event in the schedule
     */
    private long gstEventIndex = -1;

    /**
     * Tracks whether GST has been reached
     */
    private boolean gstReached = false;

    /**
     * Tracks whether a value has been committed after GST
     */
    private boolean valueCommittedAfterGst = false;

    private String explanation = "";

    public BoundedLivenessPredicate(Scenario scenario) {
        super(scenario);
        scenario.addObserver(this);
        scenario.getTransport().addObserver(this);
        scenario.getReplicas().values().forEach(replica -> replica.addObserver(this));
    }

    @Override
    public String getId() {
        return "Bounded Liveness";
    }

    public long eventsSinceGst(Scenario scenario) {
        if (!gstReached) {
            return 0; // GST not reached yet
        }
        return scenario.getSchedule().getLength() - this.gstEventIndex;
    }

    @Override
    public boolean test(Scenario scenario) {
        // If we are before GST, the scenario is considered live
        if (!gstReached) {
            this.explanation = "We are before GST";
            return true;
        }

        // If we have committed a value after GST, the scenario is considered live
        if (this.valueCommittedAfterGst) {
            this.explanation = "A value has been committed after GST";
            return true;
        }

        long eventsSinceGst = eventsSinceGst(scenario);

        // Check if we have exceeded the allowed number of events since GST
        int gstGracePeriod = ApplicationContextProvider.getConfig().getGstGracePeriod();
        if (eventsSinceGst > gstGracePeriod) {
            this.explanation = String.format("Liveness violated: %d events since GST without a committed value (max allowed: %d)", eventsSinceGst, gstGracePeriod);
            return false;
        }

        // Otherwise, the scenario is considered live... for now
        this.explanation = String.format("Grace period: %d events since GST (max allowed: %d)", eventsSinceGst, gstGracePeriod);
        return true;
    }

    @Override
    public void onLeaderChange(Replica r, String newLeaderId) {
        // no action needed
    }

    @Override
    public void onLocalCommit(Replica r, Serializable operation) {
        if (this.gstReached) {
            // A value has been committed after GST
            // We can consider the scenario live!
            this.valueCommittedAfterGst = true;
        }
    }

    @Override
    public void onTimeout(Replica r) {
        // no action needed
    }

    @Override
    public void onReplicaAdded(Replica replica) {
        replica.addObserver(this);
    }

    @Override
    public void onClientAdded(Client client) {
        // no action needed
    }

    @Override
    public void onEventAdded(Event event) {
        // no action needed
    }

    @Override
    public void onEventDropped(Event event) {
        // no action needed
    }

    @Override
    public void onEventRequeued(Event event) {
        // no action needed
    }

    @Override
    public void onEventDelivered(Event event) {
        // no action needed
    }

    @Override
    public void onMessageMutation(MutateMessageEventPayload payload) {
        // no action needed
    }

    @Override
    public void onFault(Fault fault) {
        // no action needed
    }

    @Override
    public void onTimeout(TimeoutEvent event) {
        // no action needed
    }

    @Override
    public void onGlobalStabilizationTime() {
        log.fine("GST Reached at event index: " + this.getScenario().getSchedule().getLength());
        this.gstReached = true;
        this.gstEventIndex = this.getScenario().getSchedule().getLength();
    }

    @Override
    public String getExplanation() {
        this.test();
        return this.explanation;
    }
}
