package byzzbench.simulator.transport;

import byzzbench.simulator.utils.NonNull;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.Instant;

@Data
@SuperBuilder
public abstract class Event implements Serializable {
    /**
     * The unique identifier of the event.
     */
    @NonNull
    private final long eventId;

    /**
     * The physical time at which the request was created.
     */
    @Builder.Default
    @NonNull
    private final Instant createdAt = Instant.now();

    /**
     * The physical time at which the request was delivered.
     */
    @NonNull
    private transient Instant deliveredAt;

    /**
     * The status of the event.
     */
    @Builder.Default
    @NonNull
    private Status status = Status.QUEUED;

    /**
     * A description of the type of event.
     *
     * @return The type of event.
     */
    public abstract String getType();

    public enum Status {
        QUEUED,
        DELIVERED,
        DROPPED
    }
}
