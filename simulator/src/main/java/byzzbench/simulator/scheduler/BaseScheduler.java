package byzzbench.simulator.scheduler;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.config.ByzzBenchConfig;
import byzzbench.simulator.service.MessageMutatorService;
import byzzbench.simulator.transport.ClientRequestEvent;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import byzzbench.simulator.transport.TimeoutEvent;
import byzzbench.simulator.utils.NonNull;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.*;

/**
 * Abstract base class for a scheduler.
 */
@RequiredArgsConstructor
public abstract class BaseScheduler implements Scheduler {
    /**
     * The remaining number of drop messages for each scenario.
     * If the number of remaining drop messages is 0, the scheduler will not drop messages.
     */
    protected final Map<Scenario, Integer> remainingDropMessages = new HashMap<>();
    /**
     * The remaining number of mutate messages for each scenario.
     * If the number of remaining mutate messages is 0, the scheduler will not mutate messages.
     */
    protected final Map<Scenario, Integer> remainingMutateMessages = new HashMap<>();
    /**
     * ByzzBench configuration
     */
    @Getter
    private final ByzzBenchConfig config;
    @NonNull
    @Getter(AccessLevel.PROTECTED)
    private final transient MessageMutatorService messageMutatorService;

    /**
     * Random number generator
     */
    protected Random rand = new Random();

    /**
     * Loads the parameters for the scheduler from a JSON object.
     *
     * @param parameters The JSON object containing the parameters for the scheduler.
     */
    public final void loadParameters(JsonNode parameters) {
        this.loadSchedulerParameters(parameters);
    }

    /**
     * Returns queued events of a specific type in the scenario
     *
     * @param scenario The scenario
     * @return The list of events
     */
    public <T extends Event> List<T> getQueuedEventsOfType(Scenario scenario, Class<T> eventClass) {
        return scenario.getTransport().getEventsInState(Event.Status.QUEUED)
                .stream()
                .filter(eventClass::isInstance)
                .map(eventClass::cast)
                .toList();
    }

    /**
     * Returns the queued message events in the scenario
     *
     * @param scenario The scenario
     * @return The list of message events
     */
    public List<MessageEvent> getQueuedMessageEvents(Scenario scenario) {
        return getQueuedEventsOfType(scenario, MessageEvent.class);
    }

    /**
     * Returns the timeout events in the scenario that can be delivered.
     *
     * @param scenario The scenario
     * @return The list of timeout events
     */
    public List<TimeoutEvent> getQueuedTimeoutEvents(Scenario scenario) {
        List<TimeoutEvent> events = getQueuedEventsOfType(scenario, TimeoutEvent.class).stream()
                .sorted(Comparator.comparing(TimeoutEvent::getExpiresAt))
                .toList();
        Map<String, TimeoutEvent> firstTimeoutForEachReplica = new HashMap<>();
        for (TimeoutEvent event : events) {
            firstTimeoutForEachReplica.putIfAbsent(event.getRecipientId(), event);
        }

        return events;
    }

    /**
     * Returns the queued client request events in the scenario
     *
     * @param scenario The scenario
     * @return the list of client request events
     */
    public List<ClientRequestEvent> getQueuedClientRequestEvents(Scenario scenario) {
        return getQueuedEventsOfType(scenario, ClientRequestEvent.class);
    }

    /**
     * Loads the subclass-specific parameters for the scheduler from a JSON object.
     *
     * @param parameters The JSON object containing the parameters for the scheduler.
     */
    protected abstract void loadSchedulerParameters(JsonNode parameters);

    /**
     * Retrieve one of the queued message events.
     *
     * @param messageEvents The list of queued message events
     * @return The next message event
     */
    public <T extends MessageEvent> T getNextMessageEvent(List<T> messageEvents) {
        switch (config.getScheduler().getExecutionMode()) {
            case SYNC -> {
                return messageEvents.stream().min(Comparator.comparing(Event::getEventId)).orElseThrow();
            }
            case ASYNC -> {
                return messageEvents.get(rand.nextInt(messageEvents.size()));
            }
            default ->
                    throw new IllegalStateException("Unknown execution mode: " + config.getScheduler().getExecutionMode());
        }
    }

    /**
     * Returns the weight of delivering a message
     *
     * @return The weight of delivering a message
     */
    public int deliverMessageWeight() {
        return config.getScheduler().getDeliverMessageWeight();
    }

    /**
     * Returns the weight of triggering a timeout
     *
     * @return The weight of triggering a timeout
     */
    public int deliverTimeoutWeight() {
        return config.getScheduler().getDeliverTimeoutWeight();
    }

    /**
     * Returns the weight of delivering a request from a client
     *
     * @return The weight of delivering a request from a client
     */
    public int deliverClientRequestWeight() {
        return config.getScheduler().getDeliverClientRequestWeight();
    }

    /**
     * Returns the weight of delivering a request from a client
     *
     * @return The weight of delivering a request from a client
     */
    public int dropMessageWeight(Scenario scenario) {
        int remaining = remainingDropMessages.computeIfAbsent(scenario, s -> config.getScheduler().getMaxDropMessages());
        return remaining > 0 ? config.getScheduler().getDropMessageWeight() : 0;
    }

    /**
     * Returns the weight of mutating and delivering a message
     *
     * @return The weight of mutating and delivering a message
     */
    public int mutateMessageWeight(Scenario scenario) {
        int remaining = remainingMutateMessages.computeIfAbsent(scenario, s -> config.getScheduler().getMaxMutateMessages());
        return remaining > 0 ? config.getScheduler().getMutateMessageWeight() : 0;
    }

    public void decreaseRemainingMutations(Scenario scenario) {
        int remaining = remainingMutateMessages.computeIfAbsent(scenario, s -> config.getScheduler().getMaxMutateMessages());
        remainingMutateMessages.put(scenario, remaining - 1);
    }

    public void decreaseRemainingDrops(Scenario scenario) {
        int remaining = remainingDropMessages.computeIfAbsent(scenario, s -> config.getScheduler().getMaxDropMessages());
        remainingDropMessages.put(scenario, remaining - 1);
    }
}
