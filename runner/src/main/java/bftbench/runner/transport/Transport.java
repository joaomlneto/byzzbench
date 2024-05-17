package bftbench.runner.transport;

import bftbench.runner.Replica;
import lombok.Getter;
import lombok.Synchronized;
import lombok.extern.java.Log;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Log
public class Transport {
    private final AtomicLong messageSeqNum = new AtomicLong(1);
    private final AtomicLong mutatorSeqNum = new AtomicLong(1);
    private final Map<String, Replica> nodes = new HashMap<>();

    @Getter(onMethod_ = {@Synchronized})
    private final Map<Long, MessageEvent> messages = new TreeMap<>();

    @Getter(onMethod_ = {@Synchronized})
    private final Map<Long, MessageMutator> mutators = new TreeMap<>();

    /**
     * Map of node id to the partition ID that the node is in.
     * Two nodes on different partitions cannot communicate.
     * Nodes without partition IDs are in the same "null" partition and can communicate with each other.
     */
    @Getter(onMethod_ = {@Synchronized})
    private final Map<String, Integer> partitions = new HashMap<>();

    public void addNode(Replica replica) {
        nodes.put(replica.getNodeId(), replica);
    }

    public void removeNode(Replica replica) {
        nodes.remove(replica.getNodeId());
    }

    public void sendMessage(String sender, MessagePayload message, String recipient) {
        this.multicast(sender, Set.of(recipient), message);
    }

    public void reset() {
        this.messageSeqNum.set(1);
        this.nodes.clear();
        this.messages.clear();
        this.mutators.clear();
    }

    public List<MessageEvent> getMessagesInState(MessageEvent.MessageStatus status) {
        return this.messages.values().stream().filter(m -> m.getStatus() == status).toList();
    }

    public void multicast(String sender, Set<String> recipients, MessagePayload payload) {
        for (String recipient : recipients) {
            long messageId = this.messageSeqNum.getAndIncrement();
            boolean nodesInSamePartition = this.partitions.getOrDefault(sender, 0).equals(this.partitions.getOrDefault(recipient, 0));
            MessageEvent messageEvent = new MessageEvent(messageId, sender, recipient, payload);
            if (nodesInSamePartition) {
                messageEvent.setStatus(MessageEvent.MessageStatus.QUEUED);
                log.info("Queued: " + sender + "->" + recipient + ": " + messageEvent);
            } else {
                messageEvent.setStatus(MessageEvent.MessageStatus.DROPPED);
                log.info("Dropped: " + sender + "->" + recipient + ": " + messageEvent);
            }
            messages.put(messageId, messageEvent);
        }
    }

    public void deliverMessage(long messageId) throws Exception {
        MessageEvent m = messages.get(messageId);
        if (m == null || m.getStatus() != MessageEvent.MessageStatus.QUEUED) {
            throw new RuntimeException("Message not found or not in QUEUED state");
        }
        m.setStatus(MessageEvent.MessageStatus.DELIVERED);
        nodes.get(m.getRecipientId()).handleMessage(m.getSenderId(), m.getPayload());
        log.info("Delivered: " + m.getSenderId() + "->" + m.getRecipientId() + ": " + m.getPayload());
    }

    public void dropMessage(long messageId) {
        MessageEvent m = messages.get(messageId);
        if (m == null || m.getStatus() != MessageEvent.MessageStatus.QUEUED) {
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
        System.out.println(this.mutators);
    }
}
