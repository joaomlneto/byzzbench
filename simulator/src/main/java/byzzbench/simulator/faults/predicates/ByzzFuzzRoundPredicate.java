package byzzbench.simulator.faults.predicates;

import byzzbench.simulator.exploration_strategy.byzzfuzz.ByzzFuzzRoundInfoOracle;
import byzzbench.simulator.exploration_strategy.byzzfuzz.MessageWithByzzFuzzRoundInfo;
import byzzbench.simulator.faults.FaultPredicate;
import byzzbench.simulator.faults.ScenarioContext;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

/**
 * Predicate that checks if the round of a message matches a given round
 * to be used as a precondition for fault injection in the ByzzFuzz algorithm.
 */
@RequiredArgsConstructor
public class ByzzFuzzRoundPredicate implements FaultPredicate {
    private final long round;
    private final ByzzFuzzRoundInfoOracle oracle;

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

        if (!(message.getPayload() instanceof MessageWithByzzFuzzRoundInfo roundMessage)) {
            return false;
        }

        long messageRound = this.oracle.getMessageRounds().getOrDefault(message.getEventId(), 0L);

        return messageRound == round;
    }
}
