package byzzbench.simulator.faults.predicates;

import byzzbench.simulator.faults.FaultContext;
import byzzbench.simulator.faults.FaultPredicate;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

/**
 * Predicate that checks if the message sender has a given id.
 */
@RequiredArgsConstructor
public class MessageSenderHasIdPredicate implements FaultPredicate {
  private final String nodeId;

  @Override
  public String getId() {
    return "MessageSenderHasIdPredicate-%s".formatted(this.nodeId);
  }

  @Override
  public String getName() {
    return "Message sender is %s".formatted(this.nodeId);
  }

  @Override
  public boolean test(FaultContext ctx) {
    Optional<Event> event = ctx.getEvent();

    if (event.isEmpty()) {
      return false;
    }

    // check if it is a message event
    if (!(event.get() instanceof MessageEvent message)) {
      return false;
    }

    // check if the sender of the message is the given node id
    return message.getSenderId().equals(nodeId);
  }
}
