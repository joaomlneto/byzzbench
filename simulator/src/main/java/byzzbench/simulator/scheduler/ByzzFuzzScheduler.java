package byzzbench.simulator.scheduler;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.faults.Fault;
import byzzbench.simulator.service.MessageMutatorService;
import byzzbench.simulator.transport.ClientRequestEvent;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import byzzbench.simulator.transport.TimeoutEvent;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * The ByzzFuzz scheduler from "Randomized Testing of Byzantine Fault Tolerant Algorithms" by
 * Levin N. Winter, Florena Buse, Daan de Graaf, Klaus von Gleissenthall, and Burcu Kulahcioglu Ozkan.
 * https://dl.acm.org/doi/10.1145/3586053
 */
@Component
@Log
@Getter
public class ByzzFuzzScheduler extends BaseScheduler {
    /**
     * Small-scope mutations to be applied to protocol messages
     */
    private final List<Fault> mutations = new ArrayList<>();
    /**
     * Random number generator
     */
    private final Random random = new Random();
    /**
     * Number of protocol rounds with process faults
     */
    private int numRoundsWithProcessFaults = 1;
    /**
     * Number of protocol rounds with network faults
     */
    private int numRoundsWithNetworkFaults = 1;
    /**
     * Number of protocol rounds among which the faults will be injected
     */
    private int numRoundsWithFaults = 3;

    public ByzzFuzzScheduler(MessageMutatorService messageMutatorService) {
        super("ByzzFuzz", messageMutatorService);
    }

    @Override
    protected void loadSchedulerParameters(JsonNode parameters) {
        System.out.println("Loading ByzzFuzz parameters:");

        if (parameters != null)
            System.out.println(parameters.toPrettyString());

        if (parameters != null && parameters.has("numRoundsWithProcessFaults")) {
            this.numRoundsWithProcessFaults = parameters.get("numRoundsWithProcessFaults").asInt();
        }

        if (parameters != null && parameters.has("numRoundsWithNetworkFaults")) {
            this.numRoundsWithNetworkFaults = parameters.get("numRoundsWithNetworkFaults").asInt();
        }

        if (parameters != null && parameters.has("numRoundsWithFaults")) {
            this.numRoundsWithFaults = parameters.get("numRoundsWithFaults").asInt();
        }
    }

    @Override
    public synchronized Optional<EventDecision> scheduleNext(Scenario scenario) throws Exception {
        // Get a random event
        List<Event> queuedEvents = scenario.getTransport().getEventsInState(Event.Status.QUEUED);

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
    public void reset() {
        // nothing to do
    }

    @Override
    public void stopDropMessages() {
        // FIXME: refactor
    }
}
