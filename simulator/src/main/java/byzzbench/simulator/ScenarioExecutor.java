package byzzbench.simulator;

import byzzbench.simulator.scheduler.BaseScheduler;
import byzzbench.simulator.scheduler.RandomScheduler;
import byzzbench.simulator.service.MessageMutatorService;
import byzzbench.simulator.service.SchedulesService;
import byzzbench.simulator.state.adob.AdobDistributedState;
import byzzbench.simulator.transport.Transport;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Abstract class for running a scenario, which consists of a set of {@link
 * Replica} and a {@link Transport} layer.
 *
 * @param <T> The type of the entries in the commit log of each {@link Replica}.
 */
@Getter
public abstract class ScenarioExecutor<T extends Serializable> {
    /**
     * The transport layer for the scenario.
     */
    protected final Transport<T> transport;
    /**
     * The scheduler for the scenario.
     */
    protected final BaseScheduler<T> scheduler;
    /**
     * The AdoB oracle for the scenario, which keeps track of the distributed state.
     */
    protected final AdobDistributedState adobOracle;
    /**
     * A unique identifier for the scenario.
     */
    private final String id;
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
        this.transport = new Transport<>(this, messageMutatorService, schedulesService);
        this.scheduler = new RandomScheduler<>(messageMutatorService, transport);
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
    }

    /**
     * Adds a node to the scenario.
     *
     * @param replica The node to add.
     */
    public void addNode(Replica<T> replica) {
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
}
