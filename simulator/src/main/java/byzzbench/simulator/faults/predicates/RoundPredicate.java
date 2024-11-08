package byzzbench.simulator.faults.predicates;

import byzzbench.simulator.faults.FaultContext;
import byzzbench.simulator.faults.FaultPredicate;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import byzzbench.simulator.transport.messages.MessageWithRound;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

/**
 * Predicate that checks if the round of a message matches a given round.
 */
@RequiredArgsConstructor
public class RoundPredicate implements FaultPredicate {
    private final long round;

    @Override
    public String getId() {
        return "RoundFaultPrecondition-%d".formatted(round);
    }

    @Override
    public String getName() {
        return "Round is %d".formatted(round);
    }

    @Override
    public boolean test(FaultContext ctx) {
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
