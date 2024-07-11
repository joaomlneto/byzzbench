package byzzbench.simulator.transport;

import java.time.Instant;
import lombok.Data;

/**
 * Event that represents a message being sent from one node to another.
 *
 * @see Event
 */
@Data
public class MessageEvent implements Event {
  /**
   * The unique identifier of the event.
   */
  private final long eventId;

  /**
   * The unique identifier of the sending node
   */
  private final String senderId;

  /**
   * The unique identifier of the receiving node
   */
  private final String recipientId;

  /**
   * The payload of the message.
   */
  private final MessagePayload payload;

  /**
   * The physical time at which the Message was created.
   */
  private final Instant createdAt = Instant.now();

  /**
   * The physical time at which the Message was delivered.
   */
  private transient Instant deliveredAt = null;

  /**
   * The status of the event.
   */
  private Status status = Status.QUEUED;
}
