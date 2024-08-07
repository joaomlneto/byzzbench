package byzzbench.simulator.transport;

import lombok.Data;

import java.time.Instant;

/**
 * Event that represents a message mutation.
 *
 * @see Event
 */
@Data
public class MutateMessageEvent implements Event {
    /**
     * The unique identifier of the event.
     */
    private final long eventId;

    /**
     * The unique identifier of the client that generated the event.
     */
    private final String senderId;

    /**
     * The unique identifier of the receiving node
     */
    private final String recipientId;

    /**
     * The payload of the request.
     */
    private final MutateMessageEventPayload payload;

    /**
     * The physical time at which the request was created.
     */
    private final Instant createdAt = Instant.now();

    /**
     * The physical time at which the request was delivered.
     */
    private transient Instant deliveredAt = null;

    /**
     * The status of the event.
     */
    private Status status = Status.QUEUED;

    public String getType() {
        return "Message Mutation";
    }
}
