package byzzbench.simulator.domain;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.ScenarioPredicate;
import byzzbench.simulator.transport.Action;
import byzzbench.simulator.utils.NonNull;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.*;

/**
 * Represents a schedule of events that can be executed by the simulator.
 */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
@Entity
@NoArgsConstructor
public class Schedule implements Serializable {
    /**
     * The list of events in the schedule.
     */
    @NonNull
    @Transient
    private final List<Action> actions = new ArrayList<>();
    /**
     * The set of invariants that are violated by this schedule.
     */
    @NonNull
    @Transient
    private final SortedSet<ScenarioPredicate> brokenInvariants = new TreeSet<>();

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;

    @NonNull
    @JsonIgnore
    @Transient
    private Scenario scenario;
    @NonNull
    private boolean isFinalized = false;

    /**
     * Creates a new schedule for the given scenario.
     *
     * @param scenario the scenario that generated this schedule.
     */
    public Schedule(Scenario scenario) {
        this.scenario = scenario;
    }

    public void appendEvent(Action action) {
        if (isFinalized) {
            throw new IllegalStateException("Cannot append event to a schedule with broken invariants");
        }
        actions.add(action);
    }

    /**
     * Marks the schedule as read-only, with the given broken invariants.
     *
     * @param brokenInvariants the set of broken invariants.
     */
    public void finalizeSchedule(Set<ScenarioPredicate> brokenInvariants) {
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
     *
     * @return true if the schedule is buggy, false otherwise.
     */
    @JsonIgnore
    public boolean isBuggy() {
        return !brokenInvariants.isEmpty();
    }

    /**
     * Returns the id of the scenario that generated this schedule.
     *
     * @return the id of the scenario that generated this schedule.
     */
    public @NonNull Scenario getScenario() {
        if (scenario == null) {
            // FIXME: should generate a scenario materializing the schedule here!
            throw new IllegalStateException("No scenario set for this schedule");
        }
        return this.scenario;
    }
}
