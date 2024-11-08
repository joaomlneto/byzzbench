package byzzbench.simulator.scheduler;

import byzzbench.simulator.Scenario;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.Serializable;
import java.util.Optional;

public interface Scheduler extends Serializable {
    /**
     * Get the id of the scheduler.
     */
    String getId();

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

    /**
     * Checks if the scheduler is currently dropping messages. FIXME: refactor
     *
     * @return true if the scheduler is currently dropping messages, false otherwise.
     */
    boolean isDropMessages();

    /**
     * Stops the scheduler from dropping messages. FIXME: refactor
     */
    void stopDropMessages();
}
