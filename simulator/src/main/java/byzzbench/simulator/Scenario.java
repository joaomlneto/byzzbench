package byzzbench.simulator;

import byzzbench.simulator.schedule.Schedule;
import byzzbench.simulator.scheduler.Scheduler;
import byzzbench.simulator.transport.Transport;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.Serializable;
import java.util.List;
import java.util.NavigableMap;
import java.util.SortedSet;

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
    NavigableMap<String, Replica> getNodes();

    /**
     * Get a replica by ID in the scenario.
     *
     * @return The replica object with the given ID.
     * @throws IllegalArgumentException If the replica ID is not found.
     */
    Replica getNode(String replicaId);

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
    default boolean invariantsHold() {
        return unsatisfiedInvariants().isEmpty();
    }
}
