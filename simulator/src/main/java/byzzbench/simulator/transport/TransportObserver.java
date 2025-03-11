package byzzbench.simulator.transport;

import byzzbench.simulator.faults.Fault;

public interface TransportObserver {
    /**
     * Called when an event is added to the transport layer.
     *
     * @param Event The event that was added.
     */
    void onEventAdded(Event Event);

    /**
     * Called when the status of an event changes to {@link Event.Status#DROPPED}.
     *
     * @param Event The event that was dropped.
     */
    void onEventDropped(Event Event);

    /**
     * Called when the status of an event changes from {@link Event.Status#DROPPED} to {@link Event.Status#QUEUED}.
     *
     * @param Event The event that was re-queued after being previously dropped.
     */
    void onEventRequeued(Event Event);

    /**
     * Called when the status of an event changes to {@link Event.Status#DELIVERED}.
     *
     * @param Event The event that was delivered.
     */
    void onEventDelivered(Event Event);

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
