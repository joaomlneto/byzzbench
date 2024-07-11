package byzzbench.simulator.scheduler;

import byzzbench.simulator.Replica;
import byzzbench.simulator.state.CommitLog;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import byzzbench.simulator.transport.Transport;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Optional;

/**
 * A scheduler that delivers events in the order they were enqueued.
 *
 * @param <T> The type of the entries in the {@link CommitLog} of each {@link Replica}.
 */
public class FifoScheduler<T extends Serializable> extends BaseScheduler<T> {
    public FifoScheduler(Transport<T> transport) {
        super(transport);
    }

    @Override
    public Optional<Event> scheduleNext() throws Exception {
        // Get the next event
        Optional<Event> event = getTransport()
                .getEventsInState(Event.Status.QUEUED)
                .stream()
                .filter(MessageEvent.class::isInstance)
                .min(Comparator.comparingLong(Event::getEventId));

        if (event.isPresent()) {
            this.getTransport().deliverEvent(event.get().getEventId());
            return event;
        } else {
            return Optional.empty();
        }
    }
}
