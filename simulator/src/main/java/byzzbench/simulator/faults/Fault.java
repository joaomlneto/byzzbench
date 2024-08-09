package byzzbench.simulator.faults;

import byzzbench.simulator.transport.MessageEvent;
import lombok.RequiredArgsConstructor;

/**
 * Class for applying a fault to a {@link MessageEvent}.
 * A fault is composed of a {@link FaultPrecondition} and a {@link
 * FaultBehavior}. The fault may be applied to a {@link MessageEvent} only if
 * the {@link FaultPrecondition} is satisfied.
 */
@RequiredArgsConstructor
public class Fault {
  private final FaultPrecondition precondition;
  private final FaultBehavior<MessageEvent> behavior;

  public void apply(MessageEvent message) {
    if (precondition.isSatisfiedBy(message)) {
      behavior.accept(message);
    }
  }
}
