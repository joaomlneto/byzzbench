package bftbench.runner.transport;

import lombok.Data;

@Data
public class MessageEvent implements Event {
    private final long eventId;
    private final String senderId;
    private final String recipientId;
    private final MessagePayload payload;
    private MessageStatus status;

    public enum MessageStatus {
        QUEUED,
        DELIVERED,
        DROPPED
    }
}
