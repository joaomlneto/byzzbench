package bftbench.runner.transport;

import java.io.Serializable;
import java.time.Instant;

public interface Event extends Serializable {
    long getEventId();

    String getSenderId();

    String getRecipientId();

    String toString();

    Instant getCreatedAt();

    Instant getDeliveredAt();
}
