package byzzbench.simulator.transport;

import byzzbench.simulator.faults.Fault;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.time.Instant;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * Event that represents a message mutation.
 *
 * @see Event
 */
@Data
@JsonTypeName("GenericFault")
@SuperBuilder
public class GenericFaultEvent implements Event {
  /**
   * The unique identifier of the event.
   */
  private final long eventId;

  /**
   * The unique identifier of the client that generated the event.
   */
  private final String senderId;

  /**
   * The unique identifier of the receiving node
   */
  private final String recipientId;

  /**
   * The payload of the request.
   */
  private final Fault payload;

  /**
   * The physical time at which the request was created.
   */
  private final Instant createdAt = Instant.now();

  /**
   * The physical time at which the request was delivered.
   */
  private transient Instant deliveredAt;

  /**
   * The status of the event.
   */
  @Builder.Default private Status status = Status.QUEUED;
}
