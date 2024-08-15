package byzzbench.simulator.transport;

import byzzbench.simulator.faults.Fault;
import java.time.Instant;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public abstract class FaultInjectionEvent implements Event {
  private final long eventId;
  private final Instant createdAt = Instant.now();
  private final Fault faultBehavior;
  private Status status = Status.DELIVERED; // Default status is DELIVERED

  @Override
  public String getSenderId() {
    // This method is not used in the simulator
    return "";
  }

  @Override
  public String getRecipientId() {
    // This method is not used in the simulator
    return "";
  }

  @Override
  public Instant getDeliveredAt() {
    // This method is not used in the simulator
    return this.createdAt;
  }

  @Override
  public void setStatus(Status status) {
    this.status = status;
  }
}
