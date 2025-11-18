package byzzbench.simulator.faults.behaviors;

import byzzbench.simulator.domain.DropMessageAction;
import byzzbench.simulator.faults.FaultBehavior;
import byzzbench.simulator.faults.ScenarioContext;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import lombok.extern.java.Log;

import java.util.Optional;

/**
 * Drops a specific message
 */
@Log
public class DropMessageBehavior implements FaultBehavior {
    private final long eventId;

    public DropMessageBehavior(long eventId) {
        this.eventId = eventId;
    }

    @Override
    public String getId() {
        return "drop-message";
    }

    @Override
    public String getName() {
        return "drop message";
    }

    @Override
    public DropMessageAction toAction(ScenarioContext context) {
        Optional<Event> event = context.getEvent();

        if (event.isEmpty()) {
            log.warning("No event to mutate");
            throw new IllegalStateException("No event to mutate");
        }

        Event e = event.get();

        if (!(e instanceof MessageEvent messageEvent)) {
            log.warning("Event is not a message event");
            throw new IllegalStateException("Event is not a message event");
        }

        return DropMessageAction.fromEvent(messageEvent);
    }

    @Deprecated
    public void accept(ScenarioContext context) {
        Optional<Event> event = context.getEvent();

        if (event.isEmpty()) {
            log.warning("No event to mutate");
            return;
        }

        Event e = event.get();

        if (!(e instanceof MessageEvent messageEvent)) {
            log.warning("Event is not a message event");
            return;
        }

        // otherwise, drop the message: the sender and recipient are in different partitions
        if (messageEvent.getStatus() != Event.Status.QUEUED) {
            throw new IllegalStateException("cannot drop message, as it is not in queued state");
        }

        context.getScenario().getTransport().dropEvent(e.getEventId());
    }
}
