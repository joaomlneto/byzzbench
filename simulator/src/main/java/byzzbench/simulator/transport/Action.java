package byzzbench.simulator.transport;

import byzzbench.simulator.utils.NonNull;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.Instant;

@Data
@SuperBuilder
public abstract class Action implements Serializable {
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

    public enum Status {
        QUEUED,
        DELIVERED,
        DROPPED
    }
}
