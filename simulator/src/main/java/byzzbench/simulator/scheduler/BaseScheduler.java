package byzzbench.simulator.scheduler;

import byzzbench.simulator.Replica;
import byzzbench.simulator.service.MessageMutatorService;
import byzzbench.simulator.state.CommitLog;
import byzzbench.simulator.transport.Transport;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.Optional;

/**
 * Abstract base class for a scheduler.
 *
 * @param <T> The type of the entries in the {@link CommitLog} of each {@link
 *     Replica}.
 */
@RequiredArgsConstructor
public abstract class BaseScheduler<T extends Serializable> {
  @Getter protected boolean dropMessages = true;

  @Getter private final String id;
  @Getter(AccessLevel.PROTECTED) private final MessageMutatorService messageMutatorService;
  @Getter(AccessLevel.PROTECTED) private final Transport<T> transport;

  public abstract Optional<EventDecision> scheduleNext() throws Exception;
  public abstract void stopDropMessages();
}
