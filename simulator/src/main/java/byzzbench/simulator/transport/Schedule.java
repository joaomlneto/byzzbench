package byzzbench.simulator.transport;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Builder
@Getter
public class Schedule {
    private final String scenario;
    private final List<Long> eventIds;
    private final Map<Long, List<Long>> eventMutations;

    public void addEvent(long eventId) {
        eventIds.add(eventId);
    }

    public void addMutation(long eventId, long mutationId) {
        eventMutations
                .computeIfAbsent(eventId, k -> new LinkedList<>())
                .add(mutationId);
    }
}
