package byzzbench.simulator.transport;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Builder
@Getter
public class Schedule {
    private final String scenario;
    private final List<Long> eventIds;

    public void addEvent(long eventId) {
        eventIds.add(eventId);
    }
}
