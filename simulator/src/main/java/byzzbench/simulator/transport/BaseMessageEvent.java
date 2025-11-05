package byzzbench.simulator.transport;

import byzzbench.simulator.faults.behaviors.MutateMessageBehavior;
import byzzbench.simulator.utils.NonNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@ToString(callSuper = true)
public abstract class BaseMessageEvent<T extends Serializable> extends Event implements MailboxEvent {
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
    private final Instant timestamp;

    /**
     * The payload of the message.
     */
    @NonNull
    private T payload;

    /**
     * List of mutations to be applied to the message.
     */
    private List<MutateMessageBehavior> mutations;
}
