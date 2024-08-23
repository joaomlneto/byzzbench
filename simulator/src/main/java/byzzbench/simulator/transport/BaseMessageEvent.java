package byzzbench.simulator.transport;

import byzzbench.simulator.utils.NonNull;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public abstract class BaseMessageEvent<T extends Serializable>
    extends BaseEvent implements MailboxEvent {
  /**
   * The unique identifier of the receiving node
   */
  @NonNull private final String recipientId;

  /**
   * The unique identifier of the client that generated the event.
   */
  @NonNull private final String senderId;

  /**
   * The payload of the message.
   */
  @NonNull private T payload;
}
