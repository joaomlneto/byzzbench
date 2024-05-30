package byzzbench.simulator.transport;

import java.io.Serializable;
import java.time.Instant;

public interface Event extends Serializable {
    long getEventId();

    String getSenderId();

    String getRecipientId();

    String toString();

    Instant getCreatedAt();

    Instant getDeliveredAt();

    EventType getType();

    Event.EventStatus getStatus();

    enum EventType {
        MESSAGE,
        TIMEOUT,
    }

    enum EventStatus {
        QUEUED,
        DELIVERED,
        DROPPED
    }
}
