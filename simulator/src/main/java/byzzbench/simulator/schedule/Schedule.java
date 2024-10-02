package byzzbench.simulator.schedule;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.ScenarioPredicate;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.utils.NonNull;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.*;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

@Getter
@Jacksonized
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class Schedule implements Serializable {
  /**
   * The list of events in the schedule.
   */
  @NonNull
  private final List<Event> events =
      Collections.synchronizedList(new ArrayList<>());
  /**
   * The set of invariants that are violated by this schedule.
   */
  @NonNull
  private final SortedSet<ScenarioPredicate> brokenInvariants = new TreeSet<>();

  @NonNull @JsonIgnore private final Scenario scenario;

  @NonNull @Builder.Default private boolean isFinalized = false;

  public void appendEvent(Event event) {
    if (isFinalized) {
      throw new IllegalStateException(
          "Cannot append event to a schedule with broken invariants");
    }
    events.add(event);
  }

  /**
   * Marks the schedule as read-only, with the given broken invariants.
   * @param brokenInvariants the set of broken invariants.
   */
  public void finalizeSchedule(Set<ScenarioPredicate> brokenInvariants) {
    isFinalized = true;
    this.brokenInvariants.addAll(brokenInvariants);
  }

  /**
   * Marks the schedule as read-only, without any broken invariants.
   */
  public void finalizeSchedule() { finalizeSchedule(Collections.emptySet()); }

  /**
   * Returns true if the schedule is buggy, i.e., it violates some invariants.
   * @return true if the schedule is buggy, false otherwise.
   */
  public boolean isBuggy() { return !brokenInvariants.isEmpty(); }

  public @NonNull String getScenarioId() { return scenario.getId(); }
}
