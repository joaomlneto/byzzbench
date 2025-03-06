package byzzbench.simulator.transport;

import byzzbench.simulator.faults.Fault;

public interface TransportObserver {
    /**
     * Called when an event is added to the transport layer.
     *
     * @param action The event that was added.
     */
    void onEventAdded(Action action);

    /**
     * Called when the status of an event changes to {@link Action.Status#DROPPED}.
     *
     * @param action The event that was dropped.
     */
    void onEventDropped(Action action);

    /**
     * Called when the status of an event changes from {@link Action.Status#DROPPED} to {@link Action.Status#QUEUED}.
     *
     * @param action The event that was re-queued after being previously dropped.
     */
    void onEventRequeued(Action action);

    /**
     * Called when the status of an event changes to {@link Action.Status#DELIVERED}.
     *
     * @param action The event that was delivered.
     */
    void onEventDelivered(Action action);

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
    void onTimeout(TimeoutAction event);
}
