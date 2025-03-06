package byzzbench.simulator.transport;

import byzzbench.simulator.utils.NonNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@ToString(callSuper = true)
public abstract class BaseMessageAction<T extends Serializable> extends BaseAction implements MailboxEvent {
    /**
     * The unique identifier of the receiving node
     */
    @NonNull
    private final String recipientId;

    /**
     * The unique identifier of the client that generated the event.
     */
    @NonNull
    private final String senderId;

    /**
     * The time the request was created.
     */
    @NonNull
    private final long timestamp;

    /**
     * The payload of the message.
     */
    @NonNull
    private T payload;
}
