package bftbench.runner.transport;

import lombok.Data;

import java.time.Instant;

@Data
public class MessageEvent implements Event {
    private final long eventId;
    private final String senderId;
    private final String recipientId;
    private final MessagePayload payload;
    private final Instant createdAt = Instant.now();
    private transient Instant deliveredAt = null;
    private MessageStatus status;

    public enum MessageStatus {
        QUEUED,
        DELIVERED,
        DROPPED
    }
}
