package byzzbench.simulator.transport;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Builder
@Getter
public class Schedule {
  private final String scenario;
  private final List<Long> eventIds;

  public void addEvent(long eventId) { eventIds.add(eventId); }
}
