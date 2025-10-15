package byzzbench.simulator.exploration_strategy;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.config.ByzzBenchConfig;
import byzzbench.simulator.domain.Action;
import byzzbench.simulator.domain.DeliverMessageAction;
import byzzbench.simulator.domain.TriggerTimeoutAction;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import byzzbench.simulator.transport.TimeoutEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Abstract base class for an exploration strategy.
 */
@RequiredArgsConstructor
@Getter
public abstract class ExplorationStrategy {
    /**
     * The remaining number of drop messages for each scenario.
     * If the number of remaining drop messages is 0, the exploration_strategy will not drop messages.
     */
    protected final Map<Scenario, Integer> remainingDropMessages = new HashMap<>();

    /**
     * The remaining number of mutate messages for each scenario.
     * If the number of remaining mutate messages is 0, the exploration_strategy will not mutate messages.
     */
    protected final Map<Scenario, Integer> remainingMutateMessages = new HashMap<>();

    /**
     * ByzzBench configuration
     */
    @Getter
    private final ByzzBenchConfig config;

    /**
     * Set of scenarios that have been initialized
     */
    private final Set<Scenario> initializedScenarios = new HashSet<>();

    /**
     * Random number generator
     */
    protected Random rand = new Random(1L); // FIXME: seed

    /**
     * Initializes the scenario if it has not yet been initialized.
     *
     * @param scenario the scenario to ensure is initialized
     */
    public void initializeScenarioIfNotYetDone(Scenario scenario) {
        if (!initializedScenarios.contains(scenario)) {
            this.initializeScenario(scenario);
            initializedScenarios.add(scenario);
        }
    }

    /**
     * Called whenever this exploration_strategy has been assigned a new scenario.
     * This allows for schedulers like ByzzFuzz to pre-schedule faults.
     *
     * @param scenario the scenario being assigned to this exploration_strategy
     */
    public abstract void initializeScenario(Scenario scenario);

    /**
     * Loads the parameters for the exploration_strategy from a JSON object.
     *
     * @param parameters The JSON object containing the parameters for the exploration_strategy.
     */
    public final void loadParameters(ExplorationStrategyParameters parameters) {
        this.loadSchedulerParameters(parameters);
    }

    /**
     * Get the ID of the Scheduler
     *
     * @return The ID of the Scheduler
     */
    public abstract String getId();

    /**
     * Schedules the next event to be delivered.
     *
     * @return The decision made by the exploration_strategy.
     */
    public abstract Optional<Action> scheduleNext(Scenario scenario);

    /**
     * Resets the state of the exploration_strategy.
     */
    public abstract void reset();

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
        // get all time out events in order of expiration (earliest first)
        List<TimeoutEvent> events = getQueuedEventsOfType(scenario, TimeoutEvent.class).stream()
                .sorted(Comparator.comparing(TimeoutEvent::getExpiresAt))
                .toList();
        // get the first (earliest-expiring) timeout event for each replica
        Map<String, TimeoutEvent> firstTimeoutForEachReplica = new HashMap<>();
        for (TimeoutEvent event : events) {
            firstTimeoutForEachReplica.putIfAbsent(event.getRecipientId(), event);
        }

        switch (scenario.getExecutionMode()) {
            case SYNC -> {
                // if some replicas have messages in their mailbox, no timeouts!
                if (!getQueuedMessageEvents(scenario).isEmpty()) {
                    return Collections.emptyList();
                }

                // get the set of replica IDs with messages in their mailbox
                Set<String> replicasWithQueuedMessagesInMailbox = getQueuedMessageEvents(scenario).stream()
                        .map(MessageEvent::getRecipientId)
                        .collect(Collectors.toSet());
                // return only timeouts for replicas without messages in their mailbox
                return firstTimeoutForEachReplica.values().stream()
                        .filter(event -> !replicasWithQueuedMessagesInMailbox.contains(event.getRecipientId()))
                        .toList();
            }
            case ASYNC -> {
                return firstTimeoutForEachReplica.values().stream().toList();
            }
            default -> throw new IllegalStateException("Unknown execution mode: " + scenario.getExecutionMode());
        }
    }

    /**
     * Loads the subclass-specific parameters for the exploration_strategy from a JSON object.
     *
     * @param parameters The JSON object containing the parameters for the exploration_strategy.
     */
    protected abstract void loadSchedulerParameters(ExplorationStrategyParameters parameters);

    /**
     * Retrieve one of the queued message events.
     *
     * @param messageEvents The list of queued message events
     * @return The next message event
     */
    public <T extends Event> T getNextMessageEvent(Scenario scenario, List<T> messageEvents) {
        switch (scenario.getExecutionMode()) {
            case SYNC -> {
                return messageEvents.stream().min(Comparator.comparing(Event::getEventId)).orElseThrow();
            }
            case ASYNC -> {
                return messageEvents.get(rand.nextInt(messageEvents.size()));
            }
            default -> throw new IllegalStateException("Unknown execution mode: " + scenario.getExecutionMode());
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
    public int dropMessageWeight(Scenario scenario) {
        // Check if GST
        if (scenario.getTransport().isGlobalStabilizationTime()) {
            return 0;
        }

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

    /**
     * Returns all available actions (deliver message, trigger timeout) in the scenario
     *
     * @param scenario The scenario
     * @return The list of available actions
     */
    public List<Action> getAvailableActions(Scenario scenario) {
        Stream<Action> messageEvents = this.getQueuedMessageEvents(scenario).stream().map(DeliverMessageAction::fromEvent);
        Stream<Action> timeoutEvents = this.getQueuedTimeoutEvents(scenario).stream().map(TriggerTimeoutAction::fromEvent);
        return Stream.concat(messageEvents, timeoutEvents).toList();
    }

    /**
     * Retrieves the exploration-strategy-specific data for the scenario.
     *
     * @param scenario The scenario
     * @return The exploration-strategy-specific data for the scenario.
     */
    public ScenarioStrategyData getScenarioStrategyData(Scenario scenario) {
        return ScenarioStrategyData.builder()
                .remainingDropMessages(this.getConfig().getScheduler().getMaxDropMessages())
                .remainingMutateMessages(this.getConfig().getScheduler().getMaxMutateMessages())
                .initializedByStrategy(this.getInitializedScenarios().contains(scenario))
                .build();
    }
}
