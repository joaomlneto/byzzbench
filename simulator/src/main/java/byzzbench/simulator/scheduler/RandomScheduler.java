package byzzbench.simulator.scheduler;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.config.ByzzBenchConfig;
import byzzbench.simulator.faults.faults.MessageMutationFault;
import byzzbench.simulator.service.MessageMutatorService;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import byzzbench.simulator.transport.TimeoutEvent;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.SortedSet;

/**
 * A scheduler that randomly selects events to deliver, drop, mutate or timeout.
 */
@Component
@Log
public class RandomScheduler extends Scheduler {
    private final Random random = new Random();

    public RandomScheduler(ByzzBenchConfig config, MessageMutatorService messageMutatorService) {
        super(config, messageMutatorService);
    }

    public <T> T getRandomElement(List<T> list) {
        return list.get(random.nextInt(list.size()));
    }

    @Override
    public String getId() {
        return "Random";
    }

    @Override
    public void initializeScenario(Scenario scenario) {
        // no initialization needed
    }

    @Override
    public synchronized Optional<EventDecision> scheduleNext(Scenario scenario) {
        // Get a random event
        List<Event> availableEvents = scenario.getTransport().getEventsInState(Event.Status.QUEUED);

        // if there are no events, return empty
        if (availableEvents.isEmpty()) {
            log.warning("No queued events (messages, timeouts)!");
            return Optional.empty();
        }

        SortedSet<String> faultyReplicaIds = scenario.getFaultyReplicaIds();

        List<TimeoutEvent> timeoutEvents = this.getQueuedTimeoutEvents(scenario);
        List<MessageEvent> messageEvents = availableEvents.stream().filter(MessageEvent.class::isInstance).map(MessageEvent.class::cast).toList();
        List<MessageEvent> mutateableMessageEvents = messageEvents.stream().filter(msg -> faultyReplicaIds.contains(msg.getSenderId())).toList();

        int timeoutWeight = timeoutEvents.size() * this.deliverTimeoutWeight();
        int deliverMessageWeight = messageEvents.size() * this.deliverMessageWeight();
        int dropMessageWeight = (messageEvents.size() * this.dropMessageWeight(scenario));
        int mutateMessageWeight = (mutateableMessageEvents.size() * this.mutateMessageWeight(scenario));
        int dieRoll = random.nextInt(timeoutWeight + deliverMessageWeight
                + dropMessageWeight + mutateMessageWeight);

        // check if we should trigger a timeout
        dieRoll -= timeoutWeight;
        if (dieRoll < 0) {
            Event timeout = getRandomElement(timeoutEvents);
            scenario.getTransport().deliverEvent(timeout.getEventId());
            EventDecision decision = new EventDecision(EventDecision.DecisionType.DELIVERED, timeout.getEventId());
            return Optional.of(decision);
        }

        // check if we should deliver a message (without changes)
        dieRoll -= deliverMessageWeight;
        if (dieRoll < 0) {
            Event message = getNextMessageEvent(messageEvents);
            scenario.getTransport().deliverEvent(message.getEventId());
            EventDecision decision = new EventDecision(EventDecision.DecisionType.DELIVERED, message.getEventId());
            return Optional.of(decision);
        }

        // check if we should drop a message sent between nodes
        dieRoll -= dropMessageWeight;
        if (dieRoll < 0) {
            Event message = getRandomElement(messageEvents);
            scenario.getTransport().dropEvent(message.getEventId());
            EventDecision decision = new EventDecision(EventDecision.DecisionType.DROPPED, message.getEventId());
            return Optional.of(decision);
        }

        // check if we should mutate-and-deliver a message sent between nodes
        dieRoll -= mutateMessageWeight;
        if (dieRoll < 0) {
            Event message = getRandomElement(mutateableMessageEvents);
            List<MessageMutationFault> mutators = this.getMessageMutatorService().getMutatorsForEvent(message);

            if (mutators.isEmpty()) {
                // no mutators, return nothing
                log.warning("No mutators available for message " + message.getEventId());
                return Optional.empty();
            }
            scenario.getTransport().applyMutation(
                    message.getEventId(),
                    getRandomElement(mutators));
            scenario.getTransport().deliverEvent(message.getEventId());

            EventDecision decision = new EventDecision(EventDecision.DecisionType.MUTATED_AND_DELIVERED, message.getEventId());
            return Optional.of(decision);
        }

        throw new IllegalStateException("This should never happen!");
    }

    @Override
    public void reset() {
        // nothing to do
    }

    @Override
    public void loadSchedulerParameters(JsonNode parameters) {
        // no parameters to load
    }
}
