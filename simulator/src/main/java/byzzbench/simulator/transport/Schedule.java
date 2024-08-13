package byzzbench.simulator.transport;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

import java.util.List;

@RequiredArgsConstructor
@Builder
@Getter
@Log
public class Schedule {
    private final String scenario;
    private final List<Long> eventIds;

    public void addEvent(long eventId) {
        eventIds.add(eventId);
    }

    public String toString() {
        String res = "Schedule: ";
        for (Long eid : eventIds) {
            res += eid + " ";
        }
        return res;
    }
}
