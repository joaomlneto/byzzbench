package byzzbench.simulator.faults;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.utils.NonNull;
import lombok.Getter;

import java.util.Optional;

/**
 * Utility class that gives context about a scenario and, if applicable, a given event.
 * This is useful for example for fault injection, where the fault context can be used to determine
 * if a fault should be injected or not and to offer a mechanism to apply its behavior.
 */
public class FaultContext {
    /**
     * The scenario that is being processed.
     */
    @Getter
    @NonNull
    private final Scenario scenario;

    /**
     * The event that is being processed. This is optional, as not all faults are event-specific.
     */
    private final Event eventOptional;

    /**
     * Creates a new fault context.
     *
     * @param scenario The scenario that is being processed.
     * @param event    The event that is being processed.
     */
    public FaultContext(@NonNull Scenario scenario, Event event) {
        this.scenario = scenario;
        this.eventOptional = event;
    }

    /**
     * Creates a new fault context without an event.
     *
     * @param scenario The scenario that is being processed.
     */
    public FaultContext(@NonNull Scenario scenario) {
        this(scenario, null);
    }

    /**
     * Returns the event that is being processed.
     *
     * @return The event that is being processed.
     */
    public Optional<Event> getEvent() {
        return Optional.ofNullable(eventOptional);
    }
}
