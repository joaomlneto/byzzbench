package byzzbench.simulator.transport;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * Event that represents a message being sent from one node to another.
 *
 * @see Event
 */
@Data
@JsonTypeName("Message")
@SuperBuilder
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
  private transient Instant deliveredAt;

  /**
   * The status of the event.
   */
  @Builder.Default
  private Status status = Status.QUEUED;
}
