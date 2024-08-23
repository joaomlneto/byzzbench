package byzzbench.simulator;

import byzzbench.simulator.scheduler.BaseScheduler;
import byzzbench.simulator.scheduler.RandomScheduler;
import byzzbench.simulator.service.MessageMutatorService;
import byzzbench.simulator.service.SchedulesService;
import byzzbench.simulator.state.AgreementPredicate;
import byzzbench.simulator.state.LivenessPredicate;
import byzzbench.simulator.state.adob.AdobDistributedState;
import byzzbench.simulator.transport.Transport;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.java.Log;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Abstract class for running a scenario, which consists of a set of {@link
 * Replica} and a {@link Transport} layer.
 */
@Getter
@ToString
@Log
public abstract class ScenarioExecutor {
    /**
     * The transport layer for the scenario.
     */
    @ToString.Exclude
    protected final Transport transport;
    /**
     * The scheduler for the scenario.
     */
    protected final BaseScheduler scheduler;
    /**
     * The AdoB oracle for the scenario, which keeps track of the distributed state.
     */
    protected final AdobDistributedState adobOracle;
    /**
     * A unique identifier for the scenario.
     */
    private final String id;
    /**
     * The invariants that must be satisfied by the scenario at all times.
     */
    private final List<Predicate<ScenarioExecutor>> invariants = List.of(new AgreementPredicate(), new LivenessPredicate());
    /**
     * The number of "regular" clients in the scenario.
     */
    @Setter
    private int numClients = 0;

    /**
     * Creates a new scenario executor
     *
     * @param id The unique identifier for the scenario.
     * @param messageMutatorService The service for mutating messages.
     * @param schedulesService The service for storing and managing schedules.
     */
    protected ScenarioExecutor(String id, MessageMutatorService messageMutatorService, SchedulesService schedulesService) {
        this.id = id;
        this.transport = new Transport(this, messageMutatorService, schedulesService);
        this.scheduler = new RandomScheduler(messageMutatorService, transport);
        this.setup();

        // AdoB must be initialized after the setup method is called
        this.adobOracle = new AdobDistributedState(this.transport.getNodeIds());
    }

    /**
     * Resets the scenario by clearing the transport layer and the set of nodes.
     */
    public void reset() {
        this.transport.reset();
        this.setupScenario();
        this.adobOracle.reset();
        this.runScenario();
        log.log(Level.INFO,"Scenario reset: %s", this.toString());
    }

    /**
     * Adds a node to the scenario.
     *
     * @param replica The node to add.
     */
    public void addNode(Replica replica) {
        this.transport.addNode(replica);
        replica.addObserver(this.adobOracle);
    }

    /**
     * Sets up the scenario by creating the clients and calling the setup method.
     */
    public final void setupScenario() {
        this.transport.createClients(this.numClients);
        this.transport.getNodes().values().forEach(Replica::initialize);
        this.setup();
    }

    public final void loadParameters(JsonNode parameters) {
        // get num clients
        if (parameters.has("numClients")) {
            this.numClients = parameters.get("numClients").asInt();
        }

        this.loadParameters(parameters);
    }

    /**
     * Loads the parameters for the scenario from a JSON object.
     * @param parameters The JSON object containing the parameters for the scenario.
     */
    public abstract void loadScenarioParameters(JsonNode parameters);

    /**
     * Logic to set up the scenario - must be implemented by subclasses.
     */
    protected abstract void setup();

    /**
     * Returns the termination condition for the scenario.
     * @return The termination condition for the scenario.
     */
    public abstract TerminationCondition getTerminationCondition();

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
     * @return True if the invariants are satisfied, false otherwise.
     */
    public final boolean invariantsHold() {
        return this.invariants.stream().allMatch(invariant -> invariant.test(this));
    }

    /**
     * Returns the invariants that are not satisfied by the scenario in its current state.
     * @return The invariants that are not satisfied by the scenario in its current state.
     */
    public final Set<Predicate<ScenarioExecutor>> unsatisfiedInvariants() {
        return this.invariants.stream().filter(invariant -> !invariant.test(this)).collect(Collectors.toSet());
    }

    public final void finalizeSchedule() {
        this.transport.getSchedule().finalizeSchedule(this.unsatisfiedInvariants());
    }

}
