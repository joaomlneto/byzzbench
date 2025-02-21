package byzzbench.simulator.scheduler;

import java.io.Serializable;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;

import byzzbench.simulator.Scenario;

public interface Scheduler extends Serializable {
    /**
     * Get the id of the scheduler.
     */
    String getId();

    /**
     * Called whenever this scheduler has been assigned a new scenario.
     * This allows for schedulers like ByzzFuzz to pre-schedule faults.
     *
     * @param scenario the scenario being assigned to this scheduler
     */
    void initializeScenario(Scenario scenario);

    /**
     * Loads the parameters for the scheduler from a JSON object.
     *
     * @param parameters The JSON object containing the parameters for the scheduler.
     */
    void loadParameters(JsonNode parameters);

    /**
     * Schedules the next event to be delivered.
     *
     * @return The decision made by the scheduler.
     * @throws Exception if an error occurs while scheduling the next event.
     */
    Optional<EventDecision> scheduleNext(Scenario scenario) throws Exception;

    /**
     * Resets the scheduler to its initial state.
     */
    void reset();
}
