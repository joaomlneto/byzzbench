package byzzbench.simulator.faults.predicates;

import byzzbench.simulator.faults.FaultContext;
import byzzbench.simulator.faults.FaultPredicate;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import java.util.Optional;
import java.util.Set;

/**
 * Predicate that checks if the message recipient is one of the given node ids.
 */
public class MessageRecipientHasIdPredicate implements FaultPredicate {
  private final Set<String> nodeIds;

  /**
   * Constructor.
   *
   * @param nodeIds The node ids to check for
   */
  public MessageRecipientHasIdPredicate(Set<String> nodeIds) {
    this.nodeIds = nodeIds;
  }

  /**
   * Constructor.
   *
   * @param nodeId The node id to check for
   */
  public MessageRecipientHasIdPredicate(String nodeId) { this(Set.of(nodeId)); }

  @Override
  public String getId() {
    return "MessageRecipientHasIdPredicate-%s".formatted(this.nodeIds);
  }

  @Override
  public String getName() {
    return "Message recipient is %s".formatted(this.nodeIds);
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

    // check if the recipient of the message is the given node id
    return nodeIds.contains(message.getRecipientId());
  }
}
