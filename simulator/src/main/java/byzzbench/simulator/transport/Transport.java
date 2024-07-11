package byzzbench.simulator.transport;

import byzzbench.simulator.Replica;
import byzzbench.simulator.faults.Fault;
import byzzbench.simulator.faults.NetworkFault;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Synchronized;
import lombok.extern.java.Log;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Transport layer for the simulator.
 * <p>
 * This class is responsible for handling events (messages and timeouts).
 * It also provides methods for sending messages, setting timeouts, and applying faults.
 *
 * @param <T> The type of the entries in the commit log of each {@link Replica}.
 */
@Log
public class Transport<T extends Serializable> {
    private final AtomicLong eventSeqNum = new AtomicLong(1);
    private final AtomicLong mutatorSeqNum = new AtomicLong(1);
    @JsonIgnore
    private final Map<String, Replica<T>> nodes = new HashMap<>();

    @Getter(onMethod_ = {@Synchronized})
    @JsonIgnore
    private final Map<Long, Event> events = new TreeMap<>();

    @Getter(onMethod_ = {@Synchronized})
    private final List<Event> schedule = new ArrayList<>();

    @Getter(onMethod_ = {@Synchronized})
    @JsonIgnore
    private final Map<Long, MessageMutator> mutators = new TreeMap<>();

    @Getter
    // initialize with a NetworkFault fault
    private final List<Fault> faults = new ArrayList<>(List.of(new NetworkFault(this)));

    /**
     * Map of node id to the partition ID that the node is in.
     * Two nodes on different partitions cannot communicate.
     * Nodes without partition IDs are in the same "null" partition and can communicate with each other.
     */
    @Getter(onMethod_ = {@Synchronized})
    private final Map<String, Integer> partitions = new HashMap<>();

    public void addNode(Replica<T> replica) {
        nodes.put(replica.getNodeId(), replica);
    }

    public void removeNode(Replica<T> replica) {
        nodes.remove(replica.getNodeId());
    }

    public void sendMessage(String sender, MessagePayload message, String recipient) {
        this.multicast(sender, Set.of(recipient), message);
    }

    public void reset() {
        this.eventSeqNum.set(1);
        this.nodes.clear();
        this.events.clear();
        this.mutators.clear();
        this.schedule.clear();
    }

    public List<Event> getEventsInState(Event.Status status) {
        return this.events.values().stream()
                .filter(m -> m.getStatus() == status).toList();
    }

    public void multicast(String sender, Set<String> recipients, MessagePayload payload) {
        for (String recipient : recipients) {
            long messageId = this.eventSeqNum.getAndIncrement();
            MessageEvent messageEvent = new MessageEvent(messageId, sender, recipient, payload);
            events.put(messageId, messageEvent);

            // go through the faults
            for (Fault fault : faults) {
                fault.apply(messageEvent);
            }
        }
    }

    public void deliverEvent(long eventId) throws Exception {
        // check if event is a message
        Event e = events.get(eventId);

        // check if null
        if (e == null) {
            throw new RuntimeException(String.format("Event %d not found", eventId));
        }

        // check if it is in QUEUED state
        if (e.getStatus() != Event.Status.QUEUED) {
            throw new RuntimeException("Event not in QUEUED state");
        }

        // deliver the event
        this.schedule.add(e);
        e.setStatus(Event.Status.DELIVERED);

        switch (e) {
            case MessageEvent m -> nodes.get(m.getRecipientId()).handleMessage(m.getSenderId(), m.getPayload());
            case TimeoutEvent t -> t.getTask().run();
            default -> throw new RuntimeException("Unknown event type");
        }

        log.info("Delivered " + e.getEventId() + ": " + e.getSenderId() + "->" + e.getRecipientId());
    }

    public void dropMessage(long messageId) {
        // check if event is a message
        Event e = events.get(messageId);

        if (!(e instanceof MessageEvent m)) {
            throw new RuntimeException(String.format("Event %d is not a message", messageId));
        }

        if (m.getStatus() != Event.Status.QUEUED) {
            throw new RuntimeException("Message not found or not in QUEUED state");
        }
        m.setStatus(Event.Status.DROPPED);
        log.info("Dropped: " + m.getSenderId() + "->" + m.getRecipientId() + ": " + m.getPayload());
    }

    public void registerMessageMutator(Class<? extends Serializable> messageClass, MessageMutator mutator) {
        this.mutators.put(mutatorSeqNum.getAndIncrement(), mutator);
    }

    public void registerMessageMutators(MessageMutatorFactory factory) {
        for (MessageMutator mutator : factory.mutators()) {
            for (Class<? extends Serializable> clazz : mutator.getInputClasses()) {
                this.registerMessageMutator(clazz, mutator);
            }
        }
        log.info("Registered Message Mutators:" + this.mutators);
    }

    public List<Map.Entry<Long, MessageMutator>> getEventMutators(long eventId) {
        Event e = events.get(eventId);

        List<Map.Entry<Long, MessageMutator>> messageMutators = mutators.entrySet()
                .stream()
                // filter mutators that can be applied to the event
                .filter(entry -> entry.getValue().getInputClasses().contains(((MessageEvent) e).getPayload().getClass()))
                .toList();

        // return their keys
        return messageMutators;
    }

    public void applyMutation(long eventId, long mutatorId) {
        Event e = events.get(eventId);
        MessageMutator mutator = mutators.get(mutatorId);

        if (e.getStatus() != Event.Status.QUEUED) {
            throw new RuntimeException("Message not found or not in QUEUED state");
        }

        if (mutator == null) {
            throw new RuntimeException("Mutator not found");
        }

        // check if mutator can be applied to the event
        if (!mutator.getInputClasses().contains(((MessageEvent) e).getPayload().getClass())) {
            throw new RuntimeException("Mutator cannot be applied to the event");
        }

        // check it is a message event!
        if (!(e instanceof MessageEvent m)) {
            throw new RuntimeException(String.format("Event %d is not a message - cannot mutate it.", eventId));
        }

        Serializable newPayload = mutator.apply(m.getPayload());
        // FIXME: the typecasting here to MessagePayload is very nasty
        MessageEvent newMessage = new MessageEvent(
                m.getEventId(),
                m.getSenderId(),
                m.getRecipientId(),
                (MessagePayload) newPayload);
        events.put(eventId, newMessage);
        log.info("Mutated: " + m.getSenderId() + "->" + m.getRecipientId() + ": " + m.getPayload() + " -> " + newPayload);
    }

    public long setTimeout(Replica<T> replica, Runnable runnable, long timeout) {
        Event e = new TimeoutEvent(
                this.eventSeqNum.getAndIncrement(),
                "TIMEOUT",
                replica.getNodeId(),
                timeout,
                runnable);
        this.events.put(e.getEventId(), e);
        log.info("Timeout set for " + replica.getNodeId() + " in " + timeout + "ms: " + e);
        return e.getEventId();
    }

    public void clearReplicaTimeouts(Replica<T> replica) {
        // get all event IDs for timeouts from this replica
        List<Long> eventIds = this.events.values().stream()
                .filter(e -> e instanceof TimeoutEvent t
                        && t.getSenderId().equals(replica.getNodeId())
                        && t.getStatus() == Event.Status.QUEUED)
                .map(Event::getEventId)
                .toList();

        // remove all event IDs
        for (Long eventId : eventIds) {
            this.events.remove(eventId);
        }
    }

    public void addFault(Fault fault) {
        this.faults.add(fault);
    }
}
