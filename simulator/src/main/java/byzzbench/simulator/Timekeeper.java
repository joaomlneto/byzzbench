package byzzbench.simulator;

import byzzbench.simulator.faults.Fault;
import byzzbench.simulator.transport.Action;
import byzzbench.simulator.transport.MutateMessageEventPayload;
import byzzbench.simulator.transport.TimeoutAction;
import byzzbench.simulator.transport.TransportObserver;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * A timekeeper that provides timestamps to the nodes in the simulator.
 */
public class Timekeeper implements Serializable, TransportObserver {
    /**
     * The increment to use each time a timestamp is requested.
     */
    public static final Duration INCREMENT = Duration.ofMillis(1);

    @JsonIgnore
    private final Scenario scenario;

    /**
     * The current time for each node
     */
    private final SortedMap<String, Instant> times = new TreeMap<>();

    /**
     * Create a new timekeeper for the given scenario.
     *
     * @param scenario the scenario to create the timekeeper for
     */
    public Timekeeper(Scenario scenario) {
        this.scenario = scenario;
        this.scenario.getTransport().addObserver(this);
    }

    /**
     * Advance the time for the given node and return the new time.
     *
     * @param node the node to advance the time for
     * @return the new time for the node
     */
    public Instant incrementAndGetTime(Node node) {
        Instant current = getTime(node);
        Instant next = current.plus(INCREMENT);
        times.put(node.getId(), next);
        return next;
    }

    /**
     * Get the current time for the given node.
     *
     * @param node the node to get the time for
     * @return the current time for the node
     */
    public Instant getTime(Node node) {
        return times.computeIfAbsent(node.getId(), k -> Instant.ofEpochMilli(0));
    }

    @Override
    public void onEventAdded(Action Action) {
        // nothing to do
    }

    @Override
    public void onEventDropped(Action Action) {
        // nothing to do
    }

    @Override
    public void onEventRequeued(Action Action) {
        // nothing to do
    }

    @Override
    public void onEventDelivered(Action Action) {
        // check if it was a timeout
        if (!(Action instanceof TimeoutAction timeoutEvent)) {
            return;
        }

        // set counter to max of its current value and the expiration time
        String nodeId = timeoutEvent.getNodeId();
        Instant expiration = timeoutEvent.getExpiresAt();
        Instant current = times.getOrDefault(nodeId, Instant.ofEpochMilli(0));
        times.put(nodeId, current.isAfter(expiration) ? current : expiration);
    }

    @Override
    public void onMessageMutation(MutateMessageEventPayload payload) {
        // nothing to do
    }

    @Override
    public void onFault(Fault fault) {
        // nothing to do
    }

    @Override
    public void onTimeout(TimeoutAction event) {
        // nothing to do
    }
}
