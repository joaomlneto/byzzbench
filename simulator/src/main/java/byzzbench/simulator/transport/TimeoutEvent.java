package byzzbench.simulator.transport;

import lombok.Data;

import java.time.Instant;

@Data
public class TimeoutEvent implements Event {
    private final long eventId;
    private final String description;
    private final String nodeId;
    private final long timeout; // in milliseconds
    private final Instant createdAt = Instant.now();
    private final transient Runnable task;
    private Status status = Status.QUEUED;
    private transient Instant deliveredAt = null;

    @Override
    public String getSenderId() {
        return nodeId;
    }

    @Override
    public String getRecipientId() {
        return nodeId;
    }
}
