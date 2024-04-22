package bftbench.runner.transport;

import lombok.Data;

import java.io.Serializable;

@Data
public class Message {
    private final long messageId;
    private final String senderId;
    private final String recipientId;
    private final Serializable message;
}
