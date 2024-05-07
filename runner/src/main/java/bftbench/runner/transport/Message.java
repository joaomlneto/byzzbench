package bftbench.runner.transport;

import lombok.Data;

import java.io.Serializable;

@Data
public class Message implements Serializable {
    private final long messageId;
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
