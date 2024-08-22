package byzzbench.simulator.schedule;

import byzzbench.simulator.ScenarioExecutor;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.utils.NonNull;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

import java.util.*;
import java.util.function.Predicate;

@Getter
@Jacksonized
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class Schedule {
    /**
     * The scenario ID that this schedule belongs to.
     */
    @NonNull
    private final String scenarioId;

    /**
     * The list of events in the schedule.
     */
    @NonNull
    private final List<Event> events = Collections.synchronizedList(new ArrayList<>());
    /**
     * The set of invariants that are violated by this schedule.
     */
    @NonNull
    private final Set<Predicate<ScenarioExecutor>> brokenInvariants = new HashSet<>();

    @NonNull
    private boolean isFinalized;

    public void appendEvent(Event event) {
        if (isFinalized) {
            throw new IllegalStateException("Cannot append event to a schedule with broken invariants");
        }
        events.add(event);
    }

    /**
     * Marks the schedule as read-only, with the given broken invariants.
     * @param brokenInvariants the set of broken invariants.
     */
    public void finalizeSchedule(Set<Predicate<ScenarioExecutor>> brokenInvariants) {
        isFinalized = true;
        this.brokenInvariants.addAll(brokenInvariants);
    }

    /**
     * Marks the schedule as read-only, without any broken invariants.
     */
    public void finalizeSchedule() {
        finalizeSchedule(Collections.emptySet());
    }

    /**
     * Returns true if the schedule is buggy, i.e., it violates some invariants.
     * @return true if the schedule is buggy, false otherwise.
     */
    public boolean isBuggy() {
        return !brokenInvariants.isEmpty();
    }
}
