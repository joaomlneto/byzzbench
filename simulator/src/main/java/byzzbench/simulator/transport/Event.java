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

    Type getType();

    Status getStatus();

    void setStatus(Status status);

    enum Type {
        MESSAGE,
        TIMEOUT,
    }

    enum Status {
        QUEUED,
        DELIVERED,
        DROPPED
    }
}
