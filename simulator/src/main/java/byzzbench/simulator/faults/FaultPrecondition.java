package byzzbench.simulator.faults;

import byzzbench.simulator.transport.MessageEvent;

public interface FaultPrecondition {
  boolean isSatisfiedBy(MessageEvent message);
}
