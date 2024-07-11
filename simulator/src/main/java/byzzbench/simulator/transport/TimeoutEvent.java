package byzzbench.simulator.transport;

import lombok.Data;

import java.time.Instant;

/**
 * Event that represents a timeout.
 *
 * @see Event
 */
@Data
public class TimeoutEvent implements Event {
    /**
     * The unique identifier of the event.
     */
    private final long eventId;

    /**
     * A human description of the event.
     */
    private final String description;

    /**
     * The unique identifier of the node that emitted (and shall receive) the event
     */
    private final String nodeId;

    /**
     * The timeout value in milliseconds.
     */
    private final long timeout; // in milliseconds

    /**
     * The physical time at which the Timeout was created.
     */
    private final Instant createdAt = Instant.now();

    /**
     * The task to be executed when the timeout expires.
     */
    private final transient Runnable task;

    /**
     * The status of the event.
     */
    private Status status = Status.QUEUED;

    /**
     * The physical time at which the Timeout was delivered.
     */
    private transient Instant deliveredAt = null;

    @Override
    public String getSenderId() {
        return nodeId;
    }

    @Override
    public String getRecipientId() {
        return nodeId;
    }
}
