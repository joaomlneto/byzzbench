package byzzbench.simulator.transport;

import byzzbench.simulator.faults.Fault;

public interface TransportObserver {
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
}
