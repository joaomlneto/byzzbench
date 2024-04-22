package bftbench.runner.transport;

import bftbench.runner.Node;
import lombok.Getter;
import lombok.Synchronized;
import lombok.extern.java.Log;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Log
public class Transport {
    private final AtomicLong messageSeqNum = new AtomicLong(1);
    private final Map<String, Node> nodes = new HashMap<>();

    @Getter(onMethod_={@Synchronized})
    private final Map<Long, Message> messages = new TreeMap<>();

    @Getter(onMethod_={@Synchronized})
    private final Map<Long, Message> queuedMessages = new HashMap<>();

    @Getter(onMethod_={@Synchronized})
    private final List<Message> deliveredMessages = new ArrayList<>();

    @Getter(onMethod_={@Synchronized})
    private final List<Message> droppedMessages = new ArrayList<>();

    private final Map<Class<? extends Serializable>, List<MessageMutator>> messageMutators = new HashMap<>();

    /**
     * Map of node id to the partition ID that the node is in.
     * Two nodes on different partitions cannot communicate.
     * Nodes without partition IDs are in the same "null" partition and can communicate with each other.
     */
    @Getter(onMethod_={@Synchronized})
    private final Map<String, Integer> partitions = new HashMap<>();

    public void addNode(Node node) {
        nodes.put(node.getNodeId(), node);
    }

    public void removeNode(Node node) {
        nodes.remove(node.getNodeId());
    }

    public void sendMessage(String sender, Serializable message, String recipient) {
        this.multicast(sender, Set.of(recipient), message);
    }

    public void reset() {
        this.messageSeqNum.set(1);
        this.nodes.clear();
        this.queuedMessages.clear();
    }

    public void multicast(String sender, Set<String> recipients, Serializable payload) {
        for (String recipient : recipients) {
            log.info("Queued: " + sender + "->" + recipient + ": " + payload);
            long messageId = this.messageSeqNum.getAndIncrement();
            Message message = new Message(messageId, sender, recipient, payload);
            // check if nodes are on the same network partition
            if (this.partitions.getOrDefault(sender, 0).equals(this.partitions.getOrDefault(recipient, 0))) {
                // they are: deliver message
                queuedMessages.put(messageId, message);
                log.info("Queued: " + sender + "->" + recipient + ": " + message);

            } else {
                // they are not: drop message
                droppedMessages.add(message);
                log.info("Dropped: " + sender + "->" + recipient + ": " + message);
            }
        }
    }

    public void deliverMessage(long messageId) throws Exception {
        Message m = queuedMessages.remove(messageId);
        if (m != null) {
            log.info("Delivered: " + m.getSenderId() + "->" + m.getRecipientId() + ": " + m.getMessage());
            deliveredMessages.add(m);
            nodes.get(m.getRecipientId()).handleMessage(m.getSenderId(), m.getMessage());
        }
    }

    public void dropMessage(long messageId) {
        Message m = queuedMessages.remove(messageId);
        if (m != null) {
            log.info("Dropped: " + m.getSenderId() + "->" + m.getRecipientId() + ": " + m.getMessage());
            droppedMessages.add(m);
        }
    }

    public void registerMessageMutator(Class<? extends Serializable> messageClass, MessageMutator mutator) {
        this.messageMutators.putIfAbsent(messageClass, new ArrayList<>());
        this.messageMutators.get(messageClass).add(mutator);
    }

    public <T extends Serializable> void registerMessageMutators(MessageMutatorFactory<T> factory) {
        for (MessageMutator<T> mutator : factory.mutators()) {
            this.registerMessageMutator(mutator.getSerializableClass(), mutator);
        }
        System.out.println(this.messageMutators);
    }
}
