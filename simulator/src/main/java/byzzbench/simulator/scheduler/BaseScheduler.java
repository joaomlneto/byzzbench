package byzzbench.simulator.scheduler;

import byzzbench.simulator.service.MessageMutatorService;
import byzzbench.simulator.transport.Transport;
import byzzbench.simulator.utils.NonNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

/**
 * Abstract base class for a scheduler.
 */
@RequiredArgsConstructor
public abstract class BaseScheduler {
  @NonNull @Getter private final String id;
  @NonNull @Getter(AccessLevel.PROTECTED) private final MessageMutatorService messageMutatorService;
  @NonNull @Getter(AccessLevel.PROTECTED) private final Transport transport;
  @Getter protected boolean dropMessages = true;

  public abstract Optional<EventDecision> scheduleNext() throws Exception;
  public abstract void stopDropMessages();
  public abstract void resetParameters();
}
