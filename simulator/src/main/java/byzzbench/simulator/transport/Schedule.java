package byzzbench.simulator.transport;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Getter
public class Schedule {
    private final List<Event> events = new ArrayList<>();
    private String scenario;

    public void appendEvent(Event event) {
        events.add(event);
    }
}
