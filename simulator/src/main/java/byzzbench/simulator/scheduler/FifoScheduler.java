package byzzbench.simulator.scheduler;

import byzzbench.simulator.Scenario;
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
public class FifoScheduler extends BaseScheduler {
    public FifoScheduler(MessageMutatorService messageMutatorService) {
        super("FIFO", messageMutatorService);
    }

    @Override
    public void initializeScenario(Scenario scenario) {
        // no initialization needed
    }

    @Override
    public Optional<EventDecision> scheduleNext(Scenario scenario) throws Exception {
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
    public void stopDropMessages() {
        this.dropMessages = false;
    }

    @Override
    public void reset() {
        this.dropMessages = true;
    }

    @Override
    public void loadSchedulerParameters(JsonNode parameters) {
        // no parameters to load
    }
}
