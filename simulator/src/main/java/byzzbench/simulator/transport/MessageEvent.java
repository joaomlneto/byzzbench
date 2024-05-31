package byzzbench.simulator.transport;

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
    private Status status = Status.QUEUED;

    @Override
    public Type getType() {
        return Type.MESSAGE;
    }
}
