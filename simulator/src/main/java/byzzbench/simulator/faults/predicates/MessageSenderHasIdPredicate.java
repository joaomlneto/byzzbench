package byzzbench.simulator.faults.predicates;

import byzzbench.simulator.faults.FaultPredicate;
import byzzbench.simulator.faults.ScenarioContext;
import byzzbench.simulator.transport.Action;
import byzzbench.simulator.transport.MessageAction;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

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
    public boolean test(ScenarioContext ctx) {
        Optional<Action> event = ctx.getEvent();

        if (event.isEmpty()) {
            return false;
        }

        // check if it is a message event
        if (!(event.get() instanceof MessageAction message)) {
            return false;
        }

        // check if the sender of the message is the given node id
        return message.getSenderId().equals(nodeId);
    }
}
