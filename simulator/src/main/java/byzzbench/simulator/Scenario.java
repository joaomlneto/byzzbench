package byzzbench.simulator;

import byzzbench.simulator.schedule.Schedule;
import byzzbench.simulator.scheduler.Scheduler;
import byzzbench.simulator.transport.Transport;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.Serializable;
import java.util.List;
import java.util.NavigableMap;
import java.util.Random;
import java.util.SortedSet;

/**
 * Represents a runnable scenario.
 */
public interface Scenario extends Serializable {
    /**
     * Get a unique identifier for the scenario.
     *
     * @return A unique identifier for the scenario.
     */
    String getId();

    /**
     * Run the scenario.
     */
    void runScenario();

    /**
     * Determine whether a replica is faulty.
     *
     * @param replicaId The ID of the replica to check.
     * @return True if the replica is faulty, false otherwise.
     */
    boolean isFaultyReplica(String replicaId);

    /**
     * Mark a replica as faulty.
     *
     * @param replicaId The ID of the replica to mark as faulty.
     */
    void markReplicaFaulty(String replicaId);

    /**
     * Get the scheduler for the scenario.
     *
     * @return The scheduler for the scenario.
     */
    Scheduler getScheduler();

    /**
     * Get the transport layer for the scenario.
     *
     * @return The transport layer for the scenario.
     */
    Transport getTransport();

    /**
     * Get the timekeeper for the scenario.
     *
     * @return The timekeeper for the scenario.
     */
    Timekeeper getTimekeeper();

    /**
     * Get the schedule for the scenario.
     *
     * @return The schedule for the scenario.
     */
    Schedule getSchedule();

    /**
     * Get the invariants that must be satisfied by the scenario at all times.
     *
     * @return The invariants that must be satisfied by the scenario at all times.
     */
    List<ScenarioPredicate> getInvariants();

    /**
     * Get the clients in the scenario.
     *
     * @return A map of client IDs to the client objects.
     */
    NavigableMap<String, Client> getClients();

    /**
     * Get the replicas in the scenario.
     *
     * @return A map of replica IDs to the replica objects.
     */
    NavigableMap<String, Replica> getReplicas();

    /**
     * Get the nodes in the scenario.
     *
     * @return A map of node IDs to the node objects.
     */
    NavigableMap<String, Node> getNodes();

    /**
     * Get a node by ID in the scenario.
     *
     * @return The node object with the given ID.
     * @throws IllegalArgumentException If the node ID is not found.
     */
    Node getNode(String nodeId);

    /**
     * Return the set of node IDs in the system visible to the given node.
     *
     * @param node The node to get the node IDs for.
     * @return The set of node IDs in the system visible to the given node.
     */
    SortedSet<String> getNodeIds(Node node);

    /**
     * Return the set of replica IDs in the system visible to the given node.
     * This excludes e.g. client nodes.
     *
     * @param node The node to get the replica IDs for.
     * @return The set of replica IDs in the system visible to the given node.
     */
    SortedSet<String> getReplicaIds(Node node);

    /**
     * Get the observers in the scenario.
     *
     * @return A list of observer objects.
     */
    List<ScenarioObserver> getObservers();

    /**
     * Load the parameters for the scenario.
     *
     * @param parameters The parameters for the scenario.
     */
    void loadParameters(JsonNode parameters);

    /**
     * Set up the scenario.
     */
    void setupScenario();

    /**
     * Check whether the scenario is finished, according to the termination condition.
     *
     * @return True if the scenario is finished, false otherwise.
     */
    boolean isTerminated();

    /**
     * Get the unsatisfied invariants for the scenario.
     *
     * @return The unsatisfied invariants for the scenario.
     */
    SortedSet<ScenarioPredicate> unsatisfiedInvariants();

    /**
     * Check whether the invariants hold for the scenario.
     *
     * @return True if the invariants hold, false otherwise.
     */
    boolean invariantsHold();

    /**
     * Get the random number generator for the scenario.
     *
     * @return The random number generator for the scenario.
     */
    Random getRandom();

    /**
     * Create a copy of a replica.
     *
     * @param replica The replica to clone.
     * @return A copy of the replica.
     */
    default Replica cloneReplica(Replica replica) {
        throw new UnsupportedOperationException("Not implemented!");
    }

    /**
     * Get the maximum number of faulty replicas that can be tolerated by the scenario
     * with the current configuration.
     *
     * @return The maximum number of faulty replicas that can be tolerated.
     */
    int maxFaultyReplicas();

    /**
     * Get the maximum number of faulty replicas that can be tolerated by the protocol
     * given the number of replicas in the system.
     *
     * @param n The number of replicas in the system.
     * @return The maximum number of faulty replicas that can be tolerated.
     */
    int maxFaultyReplicas(int n);

    /**
     * Get the set of IDs of replicas that have deviated from specification.
     *
     * @return The set of IDs of faulty replicas.
     */
    SortedSet<String> getFaultyReplicaIds();
}
