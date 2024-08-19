package byzzbench.simulator.transport;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.time.Instant;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * Event that represents a timeout.
 *
 * @see Event
 */
@Data
@JsonTypeName("Timeout")
@SuperBuilder
public class TimeoutEvent implements Event {
  /**
   * The unique identifier of the event.
   */
  private final long eventId;

  /**
   * A human description of the event.
   */
  private final String description;

  /**
   * The unique identifier of the node that emitted (and shall receive) the
   * event
   */
  private final String nodeId;

  /**
   * The timeout value in milliseconds.
   */
  private final long timeout; // in milliseconds

  /**
   * The physical time at which the Timeout was created.
   */
  private final Instant createdAt = Instant.now();

  /**
   * The task to be executed when the timeout expires.
   */
  private final transient Runnable task;

  /**
   * The status of the event.
   */
  @Builder.Default private Status status = Status.QUEUED;

  /**
   * The physical time at which the Timeout was delivered.
   */
  private transient Instant deliveredAt;

  @Override
  public String getSenderId() {
    return nodeId;
  }

  @Override
  public String getRecipientId() {
    return nodeId;
  }
}
