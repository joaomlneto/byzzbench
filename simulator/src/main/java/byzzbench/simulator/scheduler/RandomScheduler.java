package byzzbench.simulator.scheduler;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.config.ByzzBenchConfig;
import byzzbench.simulator.faults.faults.MessageMutationFault;
import byzzbench.simulator.service.MessageMutatorService;
import byzzbench.simulator.transport.ClientRequestEvent;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import byzzbench.simulator.transport.TimeoutEvent;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * A scheduler that randomly selects events to deliver, drop, mutate or timeout.
 */
@Component
@Log
public class RandomScheduler extends BaseScheduler {
    private final Random random = new Random(2137L);


    public RandomScheduler(ByzzBenchConfig config, MessageMutatorService messageMutatorService) {
        super(config, messageMutatorService);
    }

    public String getId() {
        return "Random";
    }

    @Override
    public void initializeScenario(Scenario scenario) {
        // no initialization needed
    }

    @Override
    public synchronized Optional<EventDecision> scheduleNext(Scenario scenario) throws Exception {
        // Get a random event
        List<Event> availableEvents = scenario.getTransport().getEventsInState(Event.Status.QUEUED);

        // if there are no events, return empty
        if (availableEvents.isEmpty()) {
            log.warning("No actions available!");
            return Optional.empty();
        }

        List<TimeoutEvent> timeoutEvents = this.getQueuedTimeoutEvents(scenario);
        List<Event> clientRequestEvents = availableEvents.stream().filter(ClientRequestEvent.class::isInstance).collect(Collectors.toList());
        List<Event> messageEvents = availableEvents.stream().filter(MessageEvent.class::isInstance).collect(Collectors.toList());

        int timeoutWeight = timeoutEvents.size() * this.deliverTimeoutWeight();
        int deliverMessageWeight = messageEvents.size() * this.deliverMessageWeight();
        int deliverClientRequestWeight = clientRequestEvents.size() * this.deliverClientRequestWeight();
        int dropMessageWeight = (messageEvents.size() * this.dropMessageWeight(scenario));
        int mutateMessageWeight = (messageEvents.size() * this.mutateMessageWeight(scenario));
        int dieRoll = random.nextInt(timeoutWeight + deliverMessageWeight
                + deliverClientRequestWeight + dropMessageWeight + mutateMessageWeight);

        // check if we should trigger a timeout
        dieRoll -= timeoutWeight;
        if (dieRoll < 0) {
            Event timeout = timeoutEvents.get(random.nextInt(timeoutEvents.size()));
            scenario.getTransport().deliverEvent(timeout.getEventId());
            EventDecision decision = new EventDecision(EventDecision.DecisionType.DELIVERED, timeout.getEventId());
            return Optional.of(decision);
        }

        // check if we should deliver a message (without changes)
        dieRoll -= deliverMessageWeight;
        if (dieRoll < 0) {
            Event message = messageEvents.get(random.nextInt(messageEvents.size()));
            scenario.getTransport().deliverEvent(message.getEventId());
            EventDecision decision = new EventDecision(EventDecision.DecisionType.DELIVERED, message.getEventId());
            return Optional.of(decision);
        }

        // check if we should target delivering a request from a client to a replica
        dieRoll -= deliverClientRequestWeight;
        if (dieRoll < 0) {
            Event request = clientRequestEvents.get(random.nextInt(clientRequestEvents.size()));
            scenario.getTransport().deliverEvent(request.getEventId());
            EventDecision decision = new EventDecision(EventDecision.DecisionType.DELIVERED, request.getEventId());
            return Optional.of(decision);
        }

        // check if we should drop a message sent between nodes
        dieRoll -= dropMessageWeight;
        if (dieRoll < 0) {
            Event message = messageEvents.get(random.nextInt(messageEvents.size()));
            scenario.getTransport().dropEvent(message.getEventId());
            EventDecision decision = new EventDecision(EventDecision.DecisionType.DROPPED, message.getEventId());
            return Optional.of(decision);
        }

        // check if we should mutate-and-deliver a message sent between nodes
        dieRoll -= mutateMessageWeight;
        if (dieRoll < 0) {
            Event message = messageEvents.get(random.nextInt(messageEvents.size()));
            List<MessageMutationFault> mutators = this.getMessageMutatorService().getMutatorsForEvent(message);

            if (mutators.isEmpty()) {
                // no mutators, return nothing
                log.warning("No mutators available for message " + message.getEventId());
                return Optional.empty();
            }
            scenario.getTransport().applyMutation(
                    message.getEventId(),
                    mutators.get(random.nextInt(mutators.size())));
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
