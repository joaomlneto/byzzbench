package byzzbench.simulator.scheduler;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.faults.faults.MessageMutationFault;
import byzzbench.simulator.service.MessageMutatorService;
import byzzbench.simulator.transport.ClientRequestEvent;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import byzzbench.simulator.transport.TimeoutEvent;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * A scheduler that randomly selects events to deliver, drop, mutate or timeout.
 */
@Component
@Log
public class RandomScheduler extends BaseScheduler {
    private final Random random = new Random();
    private double initialDropMessageProbability = RandomSchedulerConfig.DROP_MESSAGE_PROBABILITY;
    private double initialMutateMessageProbability = RandomSchedulerConfig.MUTATE_MESSAGE_PROBABILITY;
    private double initialDeliverMessageProbability = RandomSchedulerConfig.DELIVER_MESSAGE_PROBABILITY;

    @Getter
    private double deliverMessageProbability = RandomSchedulerConfig.DELIVER_MESSAGE_PROBABILITY;
    @Getter
    private double dropMessageProbability = RandomSchedulerConfig.DROP_MESSAGE_PROBABILITY;
    @Getter
    private double mutateMessageProbability = RandomSchedulerConfig.MUTATE_MESSAGE_PROBABILITY;


    public RandomScheduler(MessageMutatorService messageMutatorService) {
        super("Random", messageMutatorService);
        assert_probabilities();
    }

    private void assert_probabilities() {
        if (deliverMessageProbability < 0 || deliverMessageProbability > 1) {
            throw new IllegalArgumentException("Invalid deliver message probability: " + deliverMessageProbability);
        }
        if (dropMessageProbability < 0 || dropMessageProbability > 1) {
            throw new IllegalArgumentException("Invalid drop message probability: " + dropMessageProbability);
        }
        if (mutateMessageProbability < 0 || mutateMessageProbability > 1) {
            throw new IllegalArgumentException("Invalid mutate message probability: " + mutateMessageProbability);
        }
        if (dropMessageProbability + mutateMessageProbability +
                deliverMessageProbability != 1) {
            throw new IllegalArgumentException("Invalid probabilities: they must sum to 1");
        }
    }

    @Override
    public synchronized Optional<EventDecision> scheduleNext(Scenario scenario) throws Exception {
        // Get a random event
        List<Event> queuedEvents =
                scenario.getTransport().getEventsInState(Event.Status.QUEUED);

        // if there are no events, return empty
        if (queuedEvents.isEmpty()) {
            System.out.println("No events!");
            return Optional.empty();
        }

        int eventCount = queuedEvents.size();
        int timeoutEventCount = (int) queuedEvents.stream().filter(TimeoutEvent.class::isInstance).count();
        int clientRequestEventCount = (int) queuedEvents.stream().filter(ClientRequestEvent.class::isInstance).count();
        int messageEventCount = eventCount - (timeoutEventCount + clientRequestEventCount);

        double timeoutEventProb = (double) timeoutEventCount / eventCount;
        double clientRequestEventProb = (double) clientRequestEventCount / eventCount;
        double messageEventProb = (double) messageEventCount / eventCount;

        assert timeoutEventProb + clientRequestEventProb + messageEventProb == 1.0;

        double dieRoll = random.nextDouble();

        // check if we should schedule a timeout
        if (dieRoll < timeoutEventProb) {
            // select a random event of type timeout
            List<Event> queuedTimeouts = queuedEvents.stream()
                    .filter(TimeoutEvent.class::isInstance)
                    .toList();

            if (queuedTimeouts.isEmpty()) {
                return Optional.empty();
            }

            Event timeout = queuedTimeouts.get(random.nextInt(queuedTimeouts.size()));
            scenario.getTransport().deliverEvent(timeout.getEventId());

            EventDecision decision = new EventDecision(EventDecision.DecisionType.DELIVERED, timeout.getEventId());
            return Optional.of(decision);
        }

        // check if we should target a message
        if (dieRoll < timeoutEventProb + messageEventProb) {
            // select a random event of type message
            List<Event> queuedMessages = queuedEvents.stream()
                    .filter(MessageEvent.class::isInstance)
                    .toList();

            // if there are no messages, return an empty action
            if (queuedMessages.isEmpty()) {
                return Optional.empty();
            }

            Event message = queuedMessages.get(random.nextInt(queuedMessages.size()));

            // check if we should drop it
            if (random.nextDouble() < dropMessageProbability) {

                scenario.getTransport().dropEvent(message.getEventId());
                EventDecision decision = new EventDecision(EventDecision.DecisionType.DROPPED, message.getEventId());

                return Optional.of(decision);
            }

            // check if should mutate and deliver it
            if (random.nextDouble() < mutateMessageProbability) {
                if (!(message instanceof MessageEvent me)) {
                    throw new IllegalArgumentException("Invalid message type");
                }
                List<MessageMutationFault> mutators =
                        this.getMessageMutatorService().getMutatorsForEvent(me);
                if (mutators.isEmpty()) {
                    // no mutators, return nothing
                    return Optional.empty();
                }
                scenario.getTransport().applyMutation(
                        message.getEventId(),
                        mutators.get(random.nextInt(mutators.size())));
                scenario.getTransport().deliverEvent(message.getEventId());

                EventDecision decision = new EventDecision(EventDecision.DecisionType.MUTATED, message.getEventId());
                return Optional.of(decision);
            }

            // deliver the message, without changes
            scenario.getTransport().deliverEvent(message.getEventId());

            EventDecision decision = new EventDecision(EventDecision.DecisionType.DELIVERED, message.getEventId());
            return Optional.of(decision);
        }

        if (dieRoll < timeoutEventProb + messageEventProb + clientRequestEventProb) {
            List<Event> queuedClientRequests = queuedEvents.stream().filter(ClientRequestEvent.class::isInstance).toList();

            if (queuedClientRequests.isEmpty()) {
                return Optional.empty();
            }

            Event request = queuedClientRequests.get(random.nextInt(clientRequestEventCount));

            scenario.getTransport().deliverEvent(request.getEventId());

            EventDecision decision = new EventDecision(EventDecision.DecisionType.DELIVERED, request.getEventId());
            return Optional.of(decision);
        }

        return Optional.empty();
    }

    @Override
    public void stopDropMessages() {
        System.out.println("Will not drop messages after this point");
        this.dropMessages = false;
        this.deliverMessageProbability += this.dropMessageProbability;
        this.dropMessageProbability = 0;
        assert_probabilities();
    }

    @Override
    public void reset() {
        this.dropMessages = true;
        deliverMessageProbability = initialDeliverMessageProbability;
        dropMessageProbability = initialDropMessageProbability;
        mutateMessageProbability = initialMutateMessageProbability;
    }

    @Override
    public void loadSchedulerParameters(JsonNode parameters) {
        if (parameters.has("deliverMessageProbability")) {
            this.initialDeliverMessageProbability = parameters.get("deliverMessageProbability").asDouble();
            this.deliverMessageProbability = parameters.get("deliverMessageProbability").asDouble();
        }
        if (parameters.has("dropMessageProbability")) {
            this.initialDropMessageProbability = parameters.get("dropMessageProbability").asDouble();
            this.dropMessageProbability = parameters.get("dropMessageProbability").asDouble();
        }
        if (parameters.has("mutateMessageProbability")) {
            this.initialMutateMessageProbability = parameters.get("mutateMessageProbability").asDouble();
            this.mutateMessageProbability = parameters.get("mutateMessageProbability").asDouble();
        }
        assert_probabilities();
    }
}
