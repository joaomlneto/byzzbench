package byzzbench.runner.transport;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.Instant;

@Schema(description = "Event",
        discriminatorProperty = "type",
        subTypes = {MessageEvent.class, TimeoutEvent.class}
)
@Serdeable
public interface Event extends Serializable {
    long getEventId();

    String getSenderId();

    String getRecipientId();

    String toString();

    Instant getCreatedAt();

    Instant getDeliveredAt();

    EventType getType();

    enum EventType {
        MESSAGE,
        TIMEOUT,
    }
}
