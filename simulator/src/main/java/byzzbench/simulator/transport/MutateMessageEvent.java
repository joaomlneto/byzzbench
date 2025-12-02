package byzzbench.simulator.transport;

import byzzbench.simulator.utils.NonNull;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * Event that represents a message mutation.
 *
 * @see Event
 */
@EqualsAndHashCode(callSuper = true)
@Data
@JsonTypeName("MutateMessage")
@SuperBuilder
@ToString(callSuper = true)
public class MutateMessageEvent extends Event {
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
    @NonNull
    private final MutateMessageEventPayload payload;

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

    @Override
    public String getType() {
        return "MutateMessage";
    }
}
