package byzzbench.simulator.faults.preconditions;

import byzzbench.simulator.faults.FaultInput;
import byzzbench.simulator.faults.FaultPredicate;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import byzzbench.simulator.transport.messages.RoundMessagePayload;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class RoundFaultPrecondition implements FaultPredicate {
  private final long round;

  @Override
  public String getId() {
    return "RoundFaultPrecondition";
  }

  @Override
  public String getName() {
    return "Round Fault Precondition";
  }

  @Override
  public boolean test(FaultInput ctx) {
    Optional<Event> event = ctx.getEvent();

    if (event.isEmpty()) {
      return false;
    }

    if (!(event.get() instanceof MessageEvent message)) {
      return false;
    }

    return message.getPayload() instanceof RoundMessagePayload roundMessage
            && roundMessage.getRound() == round;
  }
}
