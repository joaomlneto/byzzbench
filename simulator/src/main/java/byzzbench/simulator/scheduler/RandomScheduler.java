package byzzbench.simulator.scheduler;

import byzzbench.simulator.transport.*;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

public class RandomScheduler<T extends Serializable> extends BaseScheduler<T> {
    private final double DELIVER_MESSAGE_PROBABILITY = 0.97;
    private final double DROP_MESSAGE_PROBABILITY = 0.01;
    private final double MUTATE_MESSAGE_PROBABILITY = 0.01;
    private final double TIMEOUT_EVENT_PROBABILITY = 0.01;
    Random random = new Random();

    public RandomScheduler(Transport<T> transport) {
        super(transport);
        assert DELIVER_MESSAGE_PROBABILITY >= 0 && DELIVER_MESSAGE_PROBABILITY <= 1;
        assert DROP_MESSAGE_PROBABILITY >= 0 && DROP_MESSAGE_PROBABILITY <= 1;
        assert MUTATE_MESSAGE_PROBABILITY >= 0 && MUTATE_MESSAGE_PROBABILITY <= 1;
        assert TIMEOUT_EVENT_PROBABILITY >= 0 && TIMEOUT_EVENT_PROBABILITY <= 1;
        assert DROP_MESSAGE_PROBABILITY + MUTATE_MESSAGE_PROBABILITY + DELIVER_MESSAGE_PROBABILITY + TIMEOUT_EVENT_PROBABILITY == 1;
    }

    @Override
    public Optional<Event> scheduleNext() throws Exception {
        // Get a random event
        List<Event> queuedEvents = getTransport()
                .getEventsInState(Event.Status.QUEUED);

        // if there are no events, return empty
        if (!queuedEvents.isEmpty()) {
            return Optional.of(queuedEvents.get(random.nextInt() * queuedEvents.size()));
        }

        double SCHEDULE_MESSAGE = DELIVER_MESSAGE_PROBABILITY + DROP_MESSAGE_PROBABILITY + MUTATE_MESSAGE_PROBABILITY;

        // check if we should schedule a timeout
        if (random.nextDouble() < TIMEOUT_EVENT_PROBABILITY) {
            // select a random event of type timeout
            List<Event> queuedTimeouts = queuedEvents
                    .stream()
                    .filter(TimeoutEvent.class::isInstance)
                    .toList();

            if (queuedTimeouts.isEmpty()) {
                return Optional.empty();
            }

            Event timeout = queuedTimeouts.get(random.nextInt() * queuedTimeouts.size());
            getTransport().deliverEvent(timeout.getEventId());
            return Optional.of(timeout);
        }

        // check if should target a message
        if (random.nextDouble() < SCHEDULE_MESSAGE) {
            // select a random event of type message
            List<Event> queuedMessages = queuedEvents
                    .stream()
                    .filter(MessageEvent.class::isInstance)
                    .toList();

            Event message = queuedMessages.get(random.nextInt() * queuedMessages.size());

            // normalize DROP probability
            double DROP_MESSAGE_PROBABILITY = this.DROP_MESSAGE_PROBABILITY / SCHEDULE_MESSAGE;
            // normalize MUTATE probability
            double MUTATE_MESSAGE_PROBABILITY = this.MUTATE_MESSAGE_PROBABILITY / SCHEDULE_MESSAGE;

            // check if should drop it
            if (random.nextDouble() < DROP_MESSAGE_PROBABILITY) {
                // FIXME: we should return a "decision" object, not just the event id we targeted!
                getTransport().dropMessage(message.getEventId());
                return Optional.of(message);
            }

            // check if should mutate and deliver it
            if (random.nextDouble() < MUTATE_MESSAGE_PROBABILITY) {
                List<Map.Entry<Long, MessageMutator>> mutators = getTransport().getEventMutators(message.getEventId());
                if (mutators.isEmpty()) {
                    // no mutators, return nothing
                    return Optional.empty();
                }
                // FIXME: we should return a "decision" object, not just the event id we targeted!
                getTransport().applyMutation(message.getEventId(), mutators.get(random.nextInt() * mutators.size()).getKey());
                getTransport().deliverEvent(message.getEventId());
                return Optional.of(message);
            }

            // deliver the message, without changes
            getTransport().deliverEvent(message.getEventId());
        }

        return Optional.empty();
    }
}
