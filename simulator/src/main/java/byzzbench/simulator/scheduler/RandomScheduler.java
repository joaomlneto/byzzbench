package byzzbench.simulator.scheduler;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.config.ByzzBenchConfig;
import byzzbench.simulator.faults.faults.MessageMutationFault;
import byzzbench.simulator.service.MessageMutatorService;
import byzzbench.simulator.transport.*;
import byzzbench.simulator.transport.Action;
import byzzbench.simulator.transport.MessageAction;
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
public class RandomScheduler extends BaseScheduler {
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
    public synchronized Optional<EventDecision> scheduleNext(Scenario scenario) throws Exception {
        // Get a random event
        List<Action> availableActions = scenario.getTransport().getEventsInState(Action.Status.QUEUED);

        // if there are no events, return empty
        if (availableActions.isEmpty()) {
            log.warning("No queued events (messages, timeouts)!");
            return Optional.empty();
        }

        List<TimeoutAction> timeoutEvents = this.getQueuedTimeoutEvents(scenario);
        List<Action> clientRequestActions = availableActions.stream().filter(ClientRequestAction.class::isInstance).toList();
        List<MessageAction> messageEvents = availableActions.stream().filter(MessageAction.class::isInstance).map(MessageAction.class::cast).toList();

        SortedSet<String> faultyReplicaIds = scenario.getFaultyReplicaIds();
        List<MessageAction> mutateableMessageEvents = messageEvents.stream().filter(msg -> faultyReplicaIds.contains(msg.getSenderId())).toList();

        int timeoutWeight = timeoutEvents.size() * this.deliverTimeoutWeight();
        int deliverMessageWeight = messageEvents.size() * this.deliverMessageWeight();
        int deliverClientRequestWeight = clientRequestActions.size() * this.deliverClientRequestWeight();
        int dropMessageWeight = (messageEvents.size() * this.dropMessageWeight(scenario));
        int mutateMessageWeight = (mutateableMessageEvents.size() * this.mutateMessageWeight(scenario));
        int dieRoll = random.nextInt(timeoutWeight + deliverMessageWeight
                + deliverClientRequestWeight + dropMessageWeight + mutateMessageWeight);

        // check if we should trigger a timeout
        dieRoll -= timeoutWeight;
        if (dieRoll < 0) {
            Action timeout = getRandomElement(timeoutEvents);
            scenario.getTransport().deliverEvent(timeout.getEventId());
            EventDecision decision = new EventDecision(EventDecision.DecisionType.DELIVERED, timeout.getEventId());
            return Optional.of(decision);
        }

        // check if we should deliver a message (without changes)
        dieRoll -= deliverMessageWeight;
        if (dieRoll < 0) {
            Action message = getNextMessageEvent(messageEvents);
            scenario.getTransport().deliverEvent(message.getEventId());
            EventDecision decision = new EventDecision(EventDecision.DecisionType.DELIVERED, message.getEventId());
            return Optional.of(decision);
        }

        // check if we should target delivering a request from a client to a replica
        dieRoll -= deliverClientRequestWeight;
        if (dieRoll < 0) {
            Action request = getRandomElement(clientRequestActions);
            scenario.getTransport().deliverEvent(request.getEventId());
            EventDecision decision = new EventDecision(EventDecision.DecisionType.DELIVERED, request.getEventId());
            return Optional.of(decision);
        }

        // check if we should drop a message sent between nodes
        dieRoll -= dropMessageWeight;
        if (dieRoll < 0) {
            Action message = getRandomElement(messageEvents);
            scenario.getTransport().dropEvent(message.getEventId());
            EventDecision decision = new EventDecision(EventDecision.DecisionType.DROPPED, message.getEventId());
            return Optional.of(decision);
        }

        // check if we should mutate-and-deliver a message sent between nodes
        dieRoll -= mutateMessageWeight;
        if (dieRoll < 0) {
            Action message = getRandomElement(mutateableMessageEvents);
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
