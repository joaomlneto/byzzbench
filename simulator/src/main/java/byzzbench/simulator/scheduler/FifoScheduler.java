package byzzbench.simulator.scheduler;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.config.ByzzBenchConfig;
import byzzbench.simulator.service.MessageMutatorService;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Optional;

/**
 * A scheduler that delivers events in the order they were enqueued.
 */
@Component
public class FifoScheduler extends Scheduler {
    public FifoScheduler(ByzzBenchConfig config, MessageMutatorService messageMutatorService) {
        super(config, messageMutatorService);
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
    public Optional<EventDecision> scheduleNext(Scenario scenario) {
        // Get the next event
        Optional<Event> event =
                scenario.getTransport()
                        .getEventsInState(Event.Status.QUEUED)
                        .stream()
                        .filter(MessageEvent.class::isInstance)
                        .min(Comparator.comparingLong(Event::getEventId));

        if (event.isPresent()) {
            scenario.getTransport().deliverEvent(event.get().getEventId());
            EventDecision decision = new EventDecision(EventDecision.DecisionType.DELIVERED, event.get().getEventId());
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
    public void loadSchedulerParameters(JsonNode parameters) {
        // no parameters to load
    }
}
