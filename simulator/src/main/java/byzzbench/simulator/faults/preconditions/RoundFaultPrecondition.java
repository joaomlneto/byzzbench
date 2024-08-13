package byzzbench.simulator.faults.preconditions;

import byzzbench.simulator.transport.MessageEvent;
import byzzbench.simulator.transport.messages.RoundMessagePayload;

import java.util.function.Predicate;

public class RoundFaultPrecondition implements Predicate<MessageEvent> {
  private final long round;

  public RoundFaultPrecondition(long round) { this.round = round; }

  @Override
  public boolean test(MessageEvent message) {
    return message.getPayload() instanceof RoundMessagePayload roundMessage
            && roundMessage.getRound() == round;
  }
}
