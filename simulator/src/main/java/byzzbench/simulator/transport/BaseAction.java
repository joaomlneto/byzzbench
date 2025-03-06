package byzzbench.simulator.transport;

import byzzbench.simulator.utils.NonNull;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Data
@SuperBuilder
public abstract class BaseAction implements Action {
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

    public void setStatus(Status status) {
        // XXX during GST, dropped messages are re-queued
        //if (this.status != Status.QUEUED) {
        //    throw new IllegalStateException("Can only change the status of a queued event");
        //}
        this.status = status;
    }
}
