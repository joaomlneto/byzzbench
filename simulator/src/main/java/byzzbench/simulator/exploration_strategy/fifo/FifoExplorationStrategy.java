package byzzbench.simulator.exploration_strategy.fifo;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.config.ByzzBenchConfig;
import byzzbench.simulator.domain.Action;
import byzzbench.simulator.domain.DeliverMessageAction;
import byzzbench.simulator.exploration_strategy.ExplorationStrategy;
import byzzbench.simulator.exploration_strategy.ExplorationStrategyParameters;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Optional;

/**
 * A exploration_strategy that delivers events in the order they were enqueued.
 */
@Component
public class FifoExplorationStrategy extends ExplorationStrategy {
    public FifoExplorationStrategy(ByzzBenchConfig config) {
        super(config);
    }

    @Override
    public String getId() {
        return "FIFO";
    }

    @Override
    public void initializeScenario(Scenario scenario) {
        // no initialization needed
    }

    @Override
    public Optional<Action> scheduleNext(Scenario scenario) {
        // Get the next event
        Optional<Event> event =
                scenario.getTransport()
                        .getEventsInState(Event.Status.QUEUED)
                        .stream()
                        .filter(MessageEvent.class::isInstance)
                        .min(Comparator.comparingLong(Event::getEventId));

        if (event.isPresent()) {
            scenario.getTransport().deliverEvent(event.get().getEventId());
            Action decision = DeliverMessageAction.builder().messageEventId(event.get().getEventId()).build();
            return Optional.of(decision);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void reset() {
        // no state to reset
    }

    @Override
    public void loadSchedulerParameters(ExplorationStrategyParameters parameters) {
        // no parameters to load
    }
}
