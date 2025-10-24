package byzzbench.simulator.transport;

import byzzbench.simulator.Node;
import byzzbench.simulator.faults.Fault;

import java.util.SortedSet;

public interface TransportObserver {
    /**
     * Called when a node unicasts a message to another node.
     * onEventAdded is still invoked for each event created (one for each recipient).
     *
     * @param sender     The node sending the message.
     * @param recipients The nodes receiving the message.
     * @param payload    The payload of the message.
     */
    void onMulticast(Node sender, SortedSet<String> recipients, MessagePayload payload);

    /**
     * Called when an event is added to the transport layer.
     *
     * @param event The event that was added.
     */
    void onEventAdded(Event event);

    /**
     * Called when the status of an event changes to {@link Event.Status#DROPPED}.
     *
     * @param event The event that was dropped.
     */
    void onEventDropped(Event event);

    /**
     * Called when the status of an event changes from {@link Event.Status#DROPPED} to {@link Event.Status#QUEUED}.
     *
     * @param event The event that was re-queued after being previously dropped.
     */
    void onEventRequeued(Event event);

    /**
     * Called when the status of an event changes to {@link Event.Status#DELIVERED}.
     *
     * @param event The event that was delivered.
     */
    void onEventDelivered(Event event);

    /**
     * Called when a message is mutated.
     *
     * @param payload The payload of the mutation.
     */
    void onMessageMutation(MutateMessageEventPayload payload);

    /**
     * Called when a fault is injected.
     *
     * @param fault The fault that was injected.
     */
    void onFault(Fault fault);

    /**
     * Called when a timeout event is created.
     *
     * @param event The timeout event that was created.
     */
    void onTimeout(TimeoutEvent event);

    /**
     * Called when Global Stabilization Time is reached.
     */
    void onGlobalStabilizationTime();

}
