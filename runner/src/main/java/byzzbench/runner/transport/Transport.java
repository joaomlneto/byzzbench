package byzzbench.runner.transport;

import byzzbench.runner.Replica;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Getter;
import lombok.Synchronized;
import lombok.extern.java.Log;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Log
@Serdeable
public class Transport<T extends Serializable> {
    private final AtomicLong eventSeqNum = new AtomicLong(1);
    private final AtomicLong mutatorSeqNum = new AtomicLong(1);
    @JsonIgnore
    private final Map<String, Replica<T>> nodes = new HashMap<>();

    @Getter(onMethod_ = {@Synchronized})
    @JsonIgnore
    private final Map<Long, Event> events = new TreeMap<>();

    @Getter(onMethod_ = {@Synchronized})
    @JsonIgnore
    private final Map<Long, MessageMutator> mutators = new TreeMap<>();

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
    }

    public List<MessageEvent> getMessagesInState(MessageEvent.MessageStatus status) {
        return this.events.values().stream()
                // filter only messages in the given state
                .filter(MessageEvent.class::isInstance)
                .map(m -> (MessageEvent) m)
                .filter(m -> m.getStatus() == status).toList();
    }

    public void multicast(String sender, Set<String> recipients, MessagePayload payload) {
        for (String recipient : recipients) {
            long messageId = this.eventSeqNum.getAndIncrement();
            boolean nodesInSamePartition = this.partitions.getOrDefault(sender, 0).equals(this.partitions.getOrDefault(recipient, 0));
            MessageEvent messageEvent = new MessageEvent(messageId, sender, recipient, payload);
            if (nodesInSamePartition) {
                messageEvent.setStatus(MessageEvent.MessageStatus.QUEUED);
                log.info("Queued: " + sender + "->" + recipient + ": " + messageEvent);
            } else {
                messageEvent.setStatus(MessageEvent.MessageStatus.DROPPED);
                log.info("Dropped: " + sender + "->" + recipient + ": " + messageEvent);
            }
            System.out.println("EVENT REGISTERED!!! " + messageEvent);
            events.put(messageId, messageEvent);
            System.out.println("EVENT REGISTERED!!! " + events);
        }
    }

    public void deliverMessage(long messageId) throws Exception {
        // check if event is a message
        Event e = events.get(messageId);

        if (!(e instanceof MessageEvent m)) {
            throw new RuntimeException(String.format("Event %d is not a message", messageId));
        }

        if (m.getStatus() != MessageEvent.MessageStatus.QUEUED) {
            throw new RuntimeException("Message not found or not in QUEUED state");
        }
        m.setStatus(MessageEvent.MessageStatus.DELIVERED);
        nodes.get(m.getRecipientId()).handleMessage(m.getSenderId(), m.getPayload());
        log.info("Delivered: " + m.getSenderId() + "->" + m.getRecipientId() + ": " + m.getPayload());
    }

    public void dropMessage(long messageId) {
        // check if event is a message
        Event e = events.get(messageId);

        if (!(e instanceof MessageEvent m)) {
            throw new RuntimeException(String.format("Event %d is not a message", messageId));
        }

        if (m.getStatus() != MessageEvent.MessageStatus.QUEUED) {
            throw new RuntimeException("Message not found or not in QUEUED state");
        }
        m.setStatus(MessageEvent.MessageStatus.DROPPED);
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

    public void setTimeout(Replica replica, Runnable runnable, long timeout) {
        Event e = new TimeoutEvent(
                this.eventSeqNum.getAndIncrement(),
                "TIMEOUT",
                replica.getNodeId(),
                timeout,
                runnable);
        this.events.put(e.getEventId(), e);
    }
}
