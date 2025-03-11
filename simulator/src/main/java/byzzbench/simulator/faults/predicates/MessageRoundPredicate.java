package byzzbench.simulator.faults.predicates;

import byzzbench.simulator.faults.FaultPredicate;
import byzzbench.simulator.faults.ScenarioContext;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import byzzbench.simulator.transport.messages.MessageWithRound;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

/**
 * Predicate that checks if the round of a message matches a given round.
 */
@RequiredArgsConstructor
public class MessageRoundPredicate implements FaultPredicate {
    private final long round;

    @Override
    public String getId() {
        return "MessageRoundFaultPrecondition-%d".formatted(round);
    }

    @Override
    public String getName() {
        return "Message Round is %d".formatted(round);
    }

    @Override
    public boolean test(ScenarioContext ctx) {
        Optional<Event> event = ctx.getEvent();

        if (event.isEmpty()) {
            return false;
        }

        if (!(event.get() instanceof MessageEvent message)) {
            return false;
        }

        return message.getPayload() instanceof MessageWithRound roundMessage
                && roundMessage.getRound() == round;
    }
}
