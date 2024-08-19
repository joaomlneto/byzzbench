package byzzbench.simulator.schedule;

import byzzbench.simulator.transport.Event;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import org.springframework.lang.NonNull;

@Getter
@Jacksonized
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class Schedule {
  /**
   * The scenario ID that this schedule belongs to.
   */
  @NonNull private final String scenarioId;

  /**
   * The list of events in the schedule.
   */
  @NonNull
  private final List<Event> events =
      Collections.synchronizedList(new ArrayList<>());
  /**
   * Whether the schedule is finalized and can no longer be modified.
   */
  @Setter private boolean isFinalized;

  public void appendEvent(Event event) {
    assert !isFinalized;
    // System.out.println("appending event with id " + event.getEventId() + ": "
    // + event);
    events.add(event);
  }
}
