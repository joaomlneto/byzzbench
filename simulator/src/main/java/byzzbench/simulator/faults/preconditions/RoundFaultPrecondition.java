package byzzbench.simulator.faults.preconditions;

import byzzbench.simulator.faults.FaultPrecondition;
import byzzbench.simulator.transport.MessageEvent;
import byzzbench.simulator.transport.messages.RoundMessage;

public class RoundFaultPrecondition implements FaultPrecondition {
  private final long round;

  public RoundFaultPrecondition(long round) { this.round = round; }

  @Override
  public boolean isSatisfiedBy(MessageEvent message) {
    if (message instanceof RoundMessage roundMessage) {
      return roundMessage.getRound() == round;
    }
    return false;
  }
}
