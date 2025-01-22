package byzzbench.simulator.scheduler;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.config.ByzzBenchConfig;
import byzzbench.simulator.faults.faults.MessageMutationFault;
import byzzbench.simulator.protocols.event_driven_hotstuff.EDHotStuffScenario;
import byzzbench.simulator.protocols.event_driven_hotstuff.messages.AbstractMessage;
import byzzbench.simulator.service.MessageMutatorService;
import byzzbench.simulator.transport.ClientRequestEvent;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import byzzbench.simulator.transport.TimeoutEvent;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;

import java.sql.Time;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

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
        List<Event> availableEvents = scenario.getTransport().getEventsInState(Event.Status.QUEUED);

        // if there are no events, return empty
        if (availableEvents.isEmpty()) {
            log.warning("No actions available!");
            return Optional.empty();
        }

        PriorityQueue<TimeoutEvent> timeoutQueue = new PriorityQueue<>(Comparator.comparing((TimeoutEvent o) -> o.getCreatedAt().plus(o.getTimeout())));
        List<TimeoutEvent> timeoutEvents = this.getQueuedTimeoutEvents(scenario);
        timeoutQueue.addAll(timeoutEvents);
        List<ClientRequestEvent> clientRequestEvents = availableEvents.stream().filter(ClientRequestEvent.class::isInstance).map(e -> (ClientRequestEvent)e).collect(Collectors.toList());
        List<MessageEvent> messageEvents = availableEvents.stream().filter(MessageEvent.class::isInstance).map(e -> (MessageEvent)e).collect(Collectors.toList());

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
            TimeoutEvent timeout = timeoutQueue.poll();//timeoutEvents.get(random.nextInt(timeoutEvents.size()));
            if(timeout != null) {
                if(scenario instanceof EDHotStuffScenario edHotStuffScenario) {
                    String targetReplicaId = timeout.getRecipientId();
                    boolean hasPendingClientRequests = !clientRequestEvents.isEmpty(); //clientRequestEvents.stream().anyMatch(cre -> cre.getRecipientId().equals(targetReplicaId));
                    boolean hasPendingMessages = !messageEvents.isEmpty(); //messageEvents.stream().anyMatch(me -> me.getRecipientId().equals(targetReplicaId));
                    if (hasPendingMessages || hasPendingClientRequests)
                        edHotStuffScenario.registerNonSyncTimeout(targetReplicaId);
                }

                scenario.getTransport().deliverEvent(timeout.getEventId());
                EventDecision decision = new EventDecision(EventDecision.DecisionType.DELIVERED, timeout.getEventId());
                return Optional.of(decision);
            }
        }

        // check if we should deliver a message (without changes)
        dieRoll -= deliverMessageWeight;
        if (dieRoll < 0) {
            Event message = getNextMessageEvent(messageEvents);
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
            MessageEvent message = messageEvents.get(random.nextInt(messageEvents.size()));
            if(scenario instanceof EDHotStuffScenario edHotStuffScenario) {
                long view = -1;
                if(message.getPayload() instanceof AbstractMessage abstractMessage) view = abstractMessage.getViewNumber();
                edHotStuffScenario.registerNetworkFault(view);
            }
            decreaseRemainingDrops(scenario);
            scenario.getTransport().dropEvent(message.getEventId());
            EventDecision decision = new EventDecision(EventDecision.DecisionType.DROPPED, message.getEventId());
            return Optional.of(decision);
        }

        // check if we should mutate-and-deliver a message sent between nodes
        dieRoll -= mutateMessageWeight;
        if (dieRoll < 0) {
            MessageEvent message = messageEvents.get(random.nextInt(messageEvents.size()));
            List<MessageMutationFault> mutators = this.getMessageMutatorService().getMutatorsForEvent(message);

            if (mutators.isEmpty()) {
                // no mutators, return nothing
                log.warning("No mutators available for message " + message.getEventId());
                return Optional.empty();
            }

            if(scenario instanceof EDHotStuffScenario edHotStuffScenario) {
                if(message.getPayload() instanceof AbstractMessage abstractMessage) {
                    long view = abstractMessage.getViewNumber();
                    edHotStuffScenario.registerFaultyReplica(view, message.getSenderId());
                }
            }

            decreaseRemainingMutations(scenario);

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
