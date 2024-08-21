package byzzbench.simulator.scheduler;

import byzzbench.simulator.service.MessageMutatorService;
import byzzbench.simulator.transport.Transport;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

/**
 * Abstract base class for a scheduler.
 */
@RequiredArgsConstructor
public abstract class BaseScheduler {
  @Getter private final String id;
  @Getter(AccessLevel.PROTECTED) private final MessageMutatorService messageMutatorService;
  @Getter(AccessLevel.PROTECTED) private final Transport transport;
  @Getter protected boolean dropMessages = true;

  public abstract Optional<EventDecision> scheduleNext() throws Exception;
  public abstract void stopDropMessages();
}
