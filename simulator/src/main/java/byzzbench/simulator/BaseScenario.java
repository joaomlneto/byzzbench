package byzzbench.simulator;

import byzzbench.simulator.faults.HealNodeNetworkFault;
import byzzbench.simulator.faults.IsolateProcessNetworkFault;
import byzzbench.simulator.schedule.Schedule;
import byzzbench.simulator.scheduler.Scheduler;
import byzzbench.simulator.state.AgreementPredicate;
import byzzbench.simulator.state.LivenessPredicate;
import byzzbench.simulator.state.adob.AdobDistributedState;
import byzzbench.simulator.transport.Transport;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Synchronized;
import lombok.ToString;
import lombok.extern.java.Log;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Abstract class for running a scenario, which consists of a set of {@link
 * Replica} and a {@link Transport} layer.
 */
@Getter
@Log
public abstract class BaseScenario implements Scenario {
    /**
     * The transport layer for the scenario.
     */
    @ToString.Exclude
    protected final transient Transport transport;

    /**
     * The timekeeper for the scenario.
     */
    protected final transient Timekeeper timekeeper;

    /**
     * The scheduler for the scenario.
     */
    protected final Scheduler scheduler;

    /**
     * Map of node id to the {@link Replica} object.
     */
    @Getter(onMethod_ = {@Synchronized})
    @JsonIgnore
    private final NavigableMap<String, Replica> nodes = new TreeMap<>();

    /**
     * Map of client id to the {@link Client} object.
     */
    @Getter(onMethod_ = {@Synchronized})
    @JsonIgnore
    private final NavigableMap<String, Client> clients = new TreeMap<>();

    /**
     * A unique identifier for the scenario.
     */
    private final String id;

    /**
     * The invariants that must be satisfied by the scenario at all times.
     */
    private final List<ScenarioPredicate> invariants = List.of(new AgreementPredicate(), new LivenessPredicate());

    /**
     * The schedule of events in order of delivery.
     */
    @Getter(onMethod_ = {@Synchronized})
    @JsonIgnore
    private final Schedule schedule = Schedule.builder().scenario(this).build();

    /**
     * The observers of this scenario.
     */
    @JsonIgnore
    private final transient List<ScenarioObserver> observers = new java.util.ArrayList<>();

    /**
     * Creates a new scenario with the given unique identifier and scheduler.
     *
     * @param id        The unique identifier for the scenario.
     * @param scheduler The scheduler for the scenario.
     */
    protected BaseScenario(String id, Scheduler scheduler) {
        this.id = id;
        this.scheduler = scheduler;
        this.transport = new Transport(this);
        this.timekeeper = new Timekeeper(this);
        this.setupScenario();
        this.addObserver(new AdobDistributedState());
    }

    /**
     * Adds an observer to the scenario.
     *
     * @param observer The observer to add.
     */
    public void addObserver(ScenarioObserver observer) {
        this.observers.add(observer);
    }

    /**
     * Removes an observer from the scenario.
     *
     * @param observer The observer to remove.
     */
    public void removeObserver(ScenarioObserver observer) {
        this.observers.remove(observer);
    }

    /**
     * Sets the number of clients in the scenario.
     *
     * @param numClients The number of clients to set.
     */
    protected void setNumClients(int numClients) {
        this.clients.clear();
        for (int i = 0; i < numClients; i++) {
            String clientId = String.format("C%d", i);
            Client client = Client.builder().clientId(clientId).transport(this.transport).build();
            this.addClient(client);
        }
    }

    /**
     * Adds a client to the scenario.
     *
     * @param client The client to add.
     */
    public void addClient(Client client) {
        this.clients.put(client.getClientId(), client);
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
        getNodes().put(replica.getNodeId(), replica);

        // for each node, add a IsolateNodeFault and a HealNodeFault
        this.transport.addNetworkFault(new IsolateProcessNetworkFault(replica.getNodeId()));
        this.transport.addNetworkFault(new HealNodeNetworkFault(replica.getNodeId()));

        // notify the observers
        this.observers.forEach(o -> o.onReplicaAdded(replica));
    }

    @Override
    public synchronized Replica getNode(String replicaId) {
        return this.getNodes().get(replicaId);
    }

    /**
     * Sets up the scenario by creating the clients and calling the setup method.
     */
    @Override
    public final void setupScenario() {
        this.setup();
        getClients().values().forEach(Client::initializeClient);
        getNodes().values().forEach(Replica::initialize);
    }

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

        this.loadScenarioParameters(parameters);
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
     * Checks if the invariants are satisfied by the scenario in its current state.
     *
     * @return True if the invariants are satisfied, false otherwise.
     */
    @Override
    public final boolean invariantsHold() {
        return this.invariants.stream().allMatch(invariant -> invariant.test(this));
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

    public final void finalizeSchedule() {
        this.getSchedule().finalizeSchedule(this.unsatisfiedInvariants());
    }

}
