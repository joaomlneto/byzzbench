package byzzbench.simulator.scheduler;

import byzzbench.simulator.Replica;
import byzzbench.simulator.state.CommitLog;
import byzzbench.simulator.transport.*;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import javax.swing.text.html.Option;

/**
 * A scheduler that randomly selects events to deliver, drop, mutate or timeout.
 *
 * @param <T> The type of the entries in the {@link CommitLog} of each {@link
 *            Replica}.
 */
public class RandomScheduler<T extends Serializable> extends BaseScheduler<T> {
    private final double DELIVER_MESSAGE_PROBABILITY = 0.9;
    private final double DROP_MESSAGE_PROBABILITY = 0.08;
    private final double MUTATE_MESSAGE_PROBABILITY = 0.02;
    Random random = new Random();

    public RandomScheduler(Transport<T> transport) {
        super(transport);
        assert DELIVER_MESSAGE_PROBABILITY >= 0 && DELIVER_MESSAGE_PROBABILITY <= 1;
        assert DROP_MESSAGE_PROBABILITY >= 0 && DROP_MESSAGE_PROBABILITY <= 1;
        assert MUTATE_MESSAGE_PROBABILITY >= 0 && MUTATE_MESSAGE_PROBABILITY <= 1;
        assert DROP_MESSAGE_PROBABILITY + MUTATE_MESSAGE_PROBABILITY +
                DELIVER_MESSAGE_PROBABILITY == 1;
    }

    @Override
    public Optional<Event> scheduleNext() throws Exception {
        // Get a random event
        List<Event> queuedEvents =
                getTransport().getEventsInState(Event.Status.QUEUED);

        // if there are no events, return empty
        if (queuedEvents.isEmpty()) {
            System.out.println("No events!");
            return Optional.empty();
        }

        int eventCount = queuedEvents.size();
        int timeoutEventCount = (int) queuedEvents.stream().filter(TimeoutEvent.class::isInstance).count();
        int clientRequestEventCount = (int) queuedEvents.stream().filter(ClientRequestEvent.class::isInstance).count();
        int clientReplyEventCount = (int) queuedEvents.stream().filter(ClientReplyEvent.class::isInstance).count();
        int messageEventCount = eventCount - (timeoutEventCount + clientReplyEventCount + clientRequestEventCount);

        double timeoutEventProb = (double) timeoutEventCount / eventCount;
        double clientRequestEventProb = (double) clientRequestEventCount / eventCount;
        double clientReplyEventProb = (double) clientReplyEventCount / eventCount;
        double messageEventProb = (double) messageEventCount / eventCount;

        assert timeoutEventProb + clientRequestEventProb + clientReplyEventProb + messageEventProb == 1.0;

        double dieRoll = random.nextDouble();

        // check if we should schedule a timeout
        if (dieRoll < timeoutEventProb) {
            // select a random event of type timeout
            List<Event> queuedTimeouts = queuedEvents.stream()
                    .filter(TimeoutEvent.class::isInstance)
                    .toList();

            if (queuedTimeouts.isEmpty()) {
                return Optional.empty();
            }

            Event timeout = queuedTimeouts.get(random.nextInt(queuedTimeouts.size()));
            getTransport().deliverEvent(timeout.getEventId());

            return Optional.of(timeout);
        }

        // check if we should target a message
        if (dieRoll < timeoutEventProb + messageEventProb) {
            // select a random event of type message
            List<Event> queuedMessages = queuedEvents.stream()
                    .filter(MessageEvent.class::isInstance)
                    .toList();

            // if there are no messages, return an empty action
            if (queuedMessages.isEmpty()) {
                return Optional.empty();
            }

            Event message = queuedMessages.get(random.nextInt(queuedMessages.size()));

            // check if we should drop it
            if (random.nextDouble() < DROP_MESSAGE_PROBABILITY) {
                // FIXME: we should return a "decision" object, not just the event id we
                // targeted!
                getTransport().dropMessage(message.getEventId());
                return Optional.of(message);
            }

            // check if should mutate and deliver it
            if (random.nextDouble() < MUTATE_MESSAGE_PROBABILITY) {
                List<Map.Entry<Long, MessageMutator>> mutators =
                        getTransport().getEventMutators(message.getEventId());
                if (mutators.isEmpty()) {
                    // no mutators, return nothing
                    return Optional.empty();
                }
                // FIXME: we should return a "decision" object, not just the event id we
                // targeted!
                getTransport().applyMutation(
                        message.getEventId(),
                        mutators.get(random.nextInt(mutators.size())).getKey());
                getTransport().deliverEvent(message.getEventId());
                return Optional.of(message);
            }

            // deliver the message, without changes
            getTransport().deliverEvent(message.getEventId());
            return Optional.of(message);
        }

        if (dieRoll < timeoutEventProb + messageEventProb + clientRequestEventProb) {
            List<Event> queuedClientRequests = queuedEvents.stream().filter(ClientRequestEvent.class::isInstance).toList();

            if (queuedClientRequests.isEmpty()) {
                return Optional.empty();
            }

            Event request = queuedClientRequests.get(random.nextInt(clientRequestEventCount));

            getTransport().deliverEvent(request.getEventId());
            return Optional.of(request);
        }

        if (dieRoll < timeoutEventProb + messageEventProb + clientRequestEventProb + clientReplyEventProb) {
            List<Event> queuedClientReplies = queuedEvents.stream().filter(ClientReplyEvent.class::isInstance).toList();

            if (queuedClientReplies.isEmpty()) {
                return Optional.empty();
            }

            Event reply = queuedClientReplies.get(random.nextInt(clientReplyEventCount));

            getTransport().deliverEvent(reply.getEventId());
            return Optional.of(reply);
        }



        return Optional.empty();
    }
}
