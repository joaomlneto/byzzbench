package byzzbench.simulator.transport;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.Instant;

/**
 * Event that represents a request from a client to a node.
 *
 * @see Event
 */
@Data
@JsonTypeName("ClientRequest")
@SuperBuilder
public class ClientRequestEvent implements Event {
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
    private final Serializable payload;

    /**
     * The physical time at which the request was created.
     */
    private final Instant createdAt = Instant.now();

    /**
     * The physical time at which the request was delivered.
     */
    private transient Instant deliveredAt;

    /**
     * The status of the event.
     */
    @Builder.Default
    private Status status = Status.QUEUED;
}
