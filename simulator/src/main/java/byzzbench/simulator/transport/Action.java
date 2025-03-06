package byzzbench.simulator.transport;

import byzzbench.simulator.utils.NonNull;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.time.Instant;

@Validated
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", visible = true)
public interface Action extends Serializable {
    /**
     * Get the eventId
     *
     * @return a long representing the eventId
     */
    @NonNull
    long getEventId();

    /**
     * Get the time at which the event was created
     *
     * @return an Instant representing the time at which the event was created
     */
    Instant getCreatedAt();

    /**
     * Get the time at which the event was delivered
     *
     * @return an Instant representing the time at which the event was delivered
     */
    Instant getDeliveredAt();

    Status getStatus();

    void setStatus(Status status);

    enum Status {
        QUEUED,
        DELIVERED,
        DROPPED
    }
}
