package byzzbench.simulator;

import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import byzzbench.simulator.transport.messages.MessageWithRound;

import java.util.List;

/**
 * A predicate that determines when a scenario should terminate.
 */
public class GenericTerminationPredicate implements ScenarioPredicate {
    private final long maxRounds = -1L;
    private final long maxEvents = -1;

    @Override
    public String getId() {
        return ScenarioPredicate.super.getId();
    }

    @Override
    public boolean test(Scenario scenario) {
        // Check if the maximum number of delivered events has been reached
        if (maxEvents > 0 && scenario.getSchedule().getEvents().size() >= maxRounds) {
            return true;
        }

        // Check if the maximum number of rounds has been reached
        if (maxRounds > 0) {
            List<MessageWithRound> queuedMessages = scenario.getTransport().getEventsInState(Event.Status.DELIVERED)
                    .stream()
                    .filter(e -> e instanceof MessageEvent msgEvent)
                    .map(e -> (MessageEvent) e)
                    .filter(e -> e.getPayload() instanceof MessageWithRound)
                    .map(e -> (MessageWithRound) e.getPayload())
                    .filter(msg -> msg.getRound() > maxRounds)
                    .toList();
            return queuedMessages.size() > 2;
        }

        return false;
    }
}
