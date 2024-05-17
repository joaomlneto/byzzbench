package bftbench.runner.transport;

import lombok.Data;

@Data
public class MessageEvent implements Event {
    private final long messageId;
    private final String senderId;
    private final String recipientId;
    private final MessagePayload payload;
    private MessageStatus status;

    @Override
    public long getEventId() {
        return this.getMessageId();
    }

    public enum MessageStatus {
        QUEUED,
        DELIVERED,
        DROPPED
    }
}
