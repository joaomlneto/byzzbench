package byzzbench.simulator.scheduler;

import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.Transport;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class RandomScheduler<T extends Serializable> extends BaseScheduler<T> {
    Random random = new Random();

    public RandomScheduler(Transport<T> transport) {
        super(transport);
    }

    @Override
    public Optional<Event> scheduleNext() throws Exception {
        // Get a random event
        List<Event> event = getTransport()
                .getEventsInState(Event.Status.QUEUED);

        // return random element from the list
        // if the list is empty, return empty
        if (!event.isEmpty()) {
            return Optional.of(event.get(random.nextInt() * event.size()));
        } else {
            return Optional.empty();
        }
    }
}
