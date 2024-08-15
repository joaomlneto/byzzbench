package byzzbench.simulator.schedule;

import byzzbench.simulator.transport.Event;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
public class Schedule {
  /**
   * The list of events in the schedule.
   */
  private final List<Event> events =
      Collections.synchronizedList(new ArrayList<>());
  /**
   * Whether the schedule is finalized and can no longer be modified.
   */
  @Setter private boolean isFinalized;

  public void appendEvent(Event event) {
    assert !isFinalized;
    System.out.println("appending event with id " + event.getEventId() + ": " +
                       event);
    events.add(event);
  }
}
