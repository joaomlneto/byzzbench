package byzzbench.simulator;

import byzzbench.simulator.domain.*;
import byzzbench.simulator.faults.Fault;
import byzzbench.simulator.faults.ScenarioContext;
import byzzbench.simulator.faults.factories.ByzzFuzzScenarioFaultFactory;
import byzzbench.simulator.faults.faults.GlobalStabilizationTimeFault;
import byzzbench.simulator.faults.faults.HealNodeNetworkFault;
import byzzbench.simulator.faults.faults.IsolateProcessNetworkFault;
import byzzbench.simulator.scheduler.Scheduler;
import byzzbench.simulator.state.AgreementPredicate;
import byzzbench.simulator.state.LivenessPredicate;
import byzzbench.simulator.state.adob.AdobDistributedState;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import byzzbench.simulator.transport.TimeoutEvent;
import byzzbench.simulator.transport.Transport;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Synchronized;
import lombok.ToString;
import lombok.extern.java.Log;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a runnable scenario.
 */
@Getter(onMethod_ = {@Synchronized})
@Log
@NoArgsConstructor
public abstract class Scenario implements Serializable {
    /**
     * Map of node id to the {@link Node} object.
     */
    @JsonIgnore
    private final NavigableMap<String, Node> nodes = new TreeMap<>();
    /**
     * The invariants that must be satisfied by the scenario at all times.
     */
    private final List<ScenarioPredicate> invariants = List.of(new AgreementPredicate(), new LivenessPredicate());
    /**
     * The schedule of events in order of delivery.
     */
    @JsonIgnore
    private final Schedule schedule = new Schedule(this);
    /**
     * The observers of this scenario.
     */
    @JsonIgnore
    private final transient List<ScenarioObserver> observers = new java.util.ArrayList<>();
    /**
     * The set of faulty replica IDs.
     */
    private final SortedSet<String> faultyReplicaIds = new TreeSet<>();
    /**
     * The termination condition for the scenario.
     */
    protected ScenarioPredicate terminationCondition;
    /**
     * Pseudo-random number generator for the scenario.
     * TODO: parameterize the seed
     */
    @Getter
    Random random = new Random(1L);
    /**
     * A unique identifier for the scenario.
     */
    @Id
    //@GeneratedValue(strategy = GenerationType.AUTO)
    private String id;
    /**
     * The transport layer for the scenario.
     */
    @ToString.Exclude
    private transient Transport transport;
    /**
     * The timekeeper for the scenario.
     */
    @JsonIgnore
    private transient Timekeeper timekeeper;
    /**
     * The scheduler for the scenario.
     */
    private Scheduler scheduler;

    /**
     * Creates a new scenario with the given unique identifier and scheduler.
     *
     * @param id        The unique identifier for the scenario.
     * @param scheduler The scheduler for the scenario.
     */
    protected Scenario(String id, Scheduler scheduler) {
        this.id = id;
        this.scheduler = scheduler;
        this.transport = new Transport(this);
        this.timekeeper = new Timekeeper(this);
        this.setupScenario();
        this.addObserver(new AdobDistributedState());
    }

    /**
     * Runs the scenario by calling the run method and initializing all nodes.
     */
    public final void runScenario() {
        this.run();
    }

    /**
     * Runs the scenario - must be implemented by subclasses.
     */
    protected abstract void run();

    /**
     * Determine whether a replica is faulty.
     *
     * @param replicaId The ID of the replica to check.
     * @return True if the replica is faulty, false otherwise.
     */
    public boolean isFaultyReplica(String replicaId) {
        return this.faultyReplicaIds.contains(replicaId);
    }

    /**
     * Mark a replica as faulty.
     *
     * @param replicaId The ID of the replica to mark as faulty.
     */
    public void markReplicaFaulty(String replicaId) {
        this.faultyReplicaIds.add(replicaId);
    }

    /**
     * Get the clients in the scenario.
     *
     * @return A map of client IDs to the client objects.
     */
    public NavigableMap<String, Client> getClients() {
        NavigableMap<String, Client> clients = new TreeMap<>();
        this.getNodes()
                .values()
                .stream()
                .filter(Client.class::isInstance)
                .map(Client.class::cast)
                .forEach(client -> clients.put(client.getId(), client));
        return clients;
    }

    /**
     * Get the replicas in the scenario.
     *
     * @return A map of replica IDs to the replica objects.
     */
    public NavigableMap<String, Replica> getReplicas() {
        NavigableMap<String, Replica> replicas = new TreeMap<>();
        this.getNodes()
                .values()
                .stream()
                .filter(Replica.class::isInstance)
                .map(Replica.class::cast)
                .forEach(replica -> replicas.put(replica.getId(), replica));
        return replicas;
    }

    /**
     * Get a node by ID in the scenario.
     *
     * @return The node object with the given ID.
     * @throws IllegalArgumentException If the node ID is not found.
     */
    public Node getNode(String nodeId) {
        return this.getNodes().get(nodeId);
    }

    /**
     * Add an observer to the scenario.
     *
     * @param observer The observer to add.
     */
    public void addObserver(ScenarioObserver observer) {
        this.observers.add(observer);
    }

    /**
     * Sets the number of clients in the scenario.
     *
     * @param numClients The number of clients to set.
     */
    protected void setNumClients(int numClients) {
        for (int i = 0; i < numClients; i++) {
            String clientId = String.format("C%d", i);
            Client client = Client.builder().id(clientId).scenario(this).build();
            this.addClient(client);
        }
    }

    /**
     * Adds a client to the scenario.
     *
     * @param client The client to add.
     */
    public void addClient(Client client) {
        this.nodes.put(client.getId(), client);
        // notify the observers
        this.observers.forEach(o -> o.onClientAdded(client));
    }

    /**
     * Adds a node to the scenario.
     *
     * @param replica The node to add.
     */
    public void addNode(Replica replica) {
        // add the node to the list of nodes
        getNodes().put(replica.getId(), replica);

        // for each node, add a IsolateNodeFault and a HealNodeFault
        this.transport.addFault(new IsolateProcessNetworkFault(replica.getId()), false);
        this.transport.addFault(new HealNodeNetworkFault(replica.getId()), false);

        // notify the observers
        this.observers.forEach(o -> o.onReplicaAdded(replica));
    }

    /**
     * Load the parameters for the scenario.
     *
     * @param parameters The parameters for the scenario.
     */
    public final void loadParameters(JsonNode parameters) {
        // get num clients
        if (parameters.has("numClients")) {
            this.setNumClients(parameters.get("numClients").asInt());
        }

        // get scheduler
        if (parameters.has("scheduler")) {
            JsonNode schedulerParameters = parameters.get("scheduler");
            this.scheduler.loadParameters(schedulerParameters);
        }

        if (parameters.has("faults")) {
            System.out.println("Faults: " + parameters.get("faults").toPrettyString());
            ByzzFuzzScenarioFaultFactory faultFactory = new ByzzFuzzScenarioFaultFactory();
            List<Fault> faults = faultFactory.generateFaults(new ScenarioContext(this));
            faults.forEach(fault -> this.transport.addFault(fault, true));
        }

        this.loadScenarioParameters(parameters);
    }

    /**
     * Get the currently queued events of a specific type in the scenario.
     *
     * @param eventClass The class of the event to get.
     * @param <T>        The type of the event.
     * @return The list of events.
     */
    private <T extends Event> Stream<T> getQueuedEventsOfType(Class<T> eventClass) {
        return getTransport().getEventsInState(Event.Status.QUEUED)
                .stream()
                .filter(eventClass::isInstance)
                .map(eventClass::cast);
    }

    /**
     * Get the available {@link DeliverMessageAction} in the scenario in the current state.
     *
     * @return The list of available {@link DeliverMessageAction}.
     */
    private List<DeliverMessageAction> getAvailableDeliverMessageAction() {
        switch (scheduler.getConfig().getScheduler().getExecutionMode()) {
            // return the first queued message in each mailbox
            case SYNC -> {
                Set<String> recipientIdsSeen = new HashSet<>();
                return getQueuedEventsOfType(MessageEvent.class)
                        .filter(event -> recipientIdsSeen.add(event.getRecipientId()))
                        .map(DeliverMessageAction::fromEvent)
                        .toList();
            }
            // return all queued message events in all mailboxes
            case ASYNC -> {
                return getQueuedEventsOfType(MessageEvent.class)
                        .map(DeliverMessageAction::fromEvent)
                        .toList();
            }
            default ->
                    throw new IllegalStateException("Unknown execution mode: " + scheduler.getConfig().getScheduler().getExecutionMode());
        }
    }

    /**
     * Get the available {@link TriggerTimeoutAction} in the scenario in the current state.
     *
     * @return The list of available {@link TriggerTimeoutAction}.
     */
    private List<TriggerTimeoutAction> getAvailableTriggerTimeoutAction() {
        // get all time out events in order of expiration (earliest first)
        Set<String> recipientIdsSeen = new HashSet<>();
        Stream<TimeoutEvent> firstTimeoutForEachReplica = getQueuedEventsOfType(TimeoutEvent.class)
                .sorted(Comparator.comparing(TimeoutEvent::getExpiresAt))
                .filter(event -> recipientIdsSeen.add(event.getRecipientId()));

        switch (this.getScheduler().getConfig().getScheduler().getExecutionMode()) {
            // return the first timeout for each replica without a message in their mailbox
            case SYNC -> {
                // return only timeouts for replicas without messages in their mailbox
                return firstTimeoutForEachReplica
                        .filter(event -> !recipientIdsSeen.contains(event.getRecipientId()))
                        .map(TriggerTimeoutAction::fromEvent)
                        .toList();
            }
            // return the first timeout for each replica
            case ASYNC -> {
                return firstTimeoutForEachReplica
                        .map(TriggerTimeoutAction::fromEvent)
                        .toList();
            }
            default ->
                    throw new IllegalStateException("Unknown execution mode: " + this.getScheduler().getConfig().getScheduler().getExecutionMode());
        }
    }

    private List<FaultInjectionAction> getAvailableFaultInjectionAction() {
        throw new UnsupportedOperationException("Not implemented!");
    }

    /**
     * Get the available actions in the scenario in the current state.
     *
     * @return The list of available actions.
     */
    public List<? extends Action> getAvailableActions() {
        return Stream.of(
                        getAvailableDeliverMessageAction().stream(),
                        getAvailableTriggerTimeoutAction().stream())
                .reduce(Stream::concat)
                .orElseGet(Stream::empty)
                .toList();

        // TODO faults
    }

    /**
     * Loads the parameters for the scenario from a JSON object.
     *
     * @param parameters The JSON object containing the parameters for the scenario.
     */
    protected abstract void loadScenarioParameters(JsonNode parameters);

    /**
     * Logic to set up the scenario - must be implemented by subclasses.
     */
    protected abstract void setup();

    /**
     * Sets up the scenario by creating the clients and calling the setup method.
     */
    public final void setupScenario() {
        this.setup();

        // sample f replicas to be faulty at start
        List<String> replicaIds = new ArrayList<>(this.getReplicas().keySet());
        Collections.shuffle(replicaIds);
        int f = this.maxFaultyReplicas();
        for (int i = 0; i < f; i++) {
            this.markReplicaFaulty(replicaIds.get(i));
        }

        //this.getClients().values().forEach(Client::initialize);
        this.getNodes().values().forEach(Node::initialize);
        this.scheduler.initializeScenario(this);
        this.transport.addFault(new GlobalStabilizationTimeFault(), false);
    }

    /**
     * Check whether the scenario is finished, according to the termination condition.
     *
     * @return True if the scenario is finished, false otherwise.
     */
    public boolean isTerminated() {
        return this.terminationCondition.test(this);
    }

    /**
     * Returns the invariants that are not satisfied by the scenario in its current state.
     *
     * @return The invariants that are not satisfied by the scenario in its current state.
     */
    public final SortedSet<ScenarioPredicate> unsatisfiedInvariants() {
        return this.invariants.stream().filter(invariant -> !invariant.test(this))
                .collect(Collectors.toCollection(TreeSet::new));
    }

    /**
     * Checks if the invariants are satisfied by the scenario in its current state.
     *
     * @return True if the invariants are satisfied, false otherwise.
     */
    public final boolean invariantsHold() {
        return this.invariants.stream()
                .allMatch(invariant -> invariant.test(this));
    }

    /**
     * Create a copy of a replica.
     *
     * @param replica The replica to clone.
     * @return A copy of the replica.
     */
    public Replica cloneReplica(Replica replica) {
        throw new UnsupportedOperationException("Not implemented!");
    }

    /**
     * Get the maximum number of faulty replicas that can be tolerated by the scenario
     * with the current configuration.
     *
     * @return The maximum number of faulty replicas that can be tolerated.
     */
    public int maxFaultyReplicas() {
        int f = this.maxFaultyReplicas(this.getReplicas().size());
        if (f < 1) {
            log.severe("Scenario does not have enough replicas to tolerate any faults!");
        }
        return f;
    }

    /**
     * Get the maximum number of faulty replicas that can be tolerated by the protocol
     * given the number of replicas in the system.
     *
     * @param n The number of replicas in the system.
     * @return The maximum number of faulty replicas that can be tolerated.
     */
    public abstract int maxFaultyReplicas(int n);

    /**
     * Return the set of replica IDs in the system visible to the given node.
     * This excludes e.g. client nodes.
     *
     * @param node The node to get the replica IDs for.
     * @return The set of replica IDs in the system visible to the given node.
     */
    public SortedSet<String> getReplicaIds(Node node) {
        return new TreeSet<>(this.getReplicas().keySet());
    }
}
