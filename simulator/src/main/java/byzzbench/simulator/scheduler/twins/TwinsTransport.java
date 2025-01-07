package byzzbench.simulator.scheduler.twins;

import byzzbench.simulator.Node;
import byzzbench.simulator.Replica;
import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.transport.Transport;
import byzzbench.simulator.transport.messages.MessageWithRound;
import lombok.Getter;
import lombok.extern.java.Log;

import java.io.Serializable;
import java.time.Duration;
import java.util.*;

/**
 * A transport layer that is used by the {@link TwinsReplica} to route messages
 * internally between the twin replica instances.
 */
@Log
public class TwinsTransport extends Transport implements Serializable {
    /**
     * Partitions to use for the replicas if another partition is not specified.
     */
    @Getter
    private final List<List<String>> defaultPartitions;

    /**
     * Scheduled partitions to use for each round.
     */
    @Getter
    private final Map<Long, List<List<String>>> roundPartitions = new TreeMap<>();

    @Getter
    private final Map<String, List<Long>> queuedTimeouts = new TreeMap<>();

    /**
     * The Twins Replica using this transport.
     */
    private final TwinsReplica twinsReplica;

    /**
     * Create a new Twins transport layer for the given Twins replica.
     *
     * @param twinsReplica the Twins replica
     */
    public TwinsTransport(TwinsReplica twinsReplica) {
        super(twinsReplica.getScenario());
        this.twinsReplica = twinsReplica;

        // By default, both internal replicas will be connected to the network.
        List<String> singlePartition = new ArrayList<>();
        singlePartition.addAll(twinsReplica.getNodeIds().stream().filter(id -> !id.equals(twinsReplica.getId())).toList());
        singlePartition.addAll(twinsReplica.getInternalIds());
        this.defaultPartitions = List.of(singlePartition);
    }


    /**
     * Get the partitions to use for a given round number
     *
     * @param roundNumber the round number
     * @return the partitions to use
     */
    public List<List<String>> getRoundPartition(long roundNumber) {
        return this.roundPartitions.getOrDefault(roundNumber, this.defaultPartitions);
    }

    /**
     * Get the partition that the given replica ID is in for the given round number.
     *
     * @param replicaId   the replica ID
     * @param roundNumber the round number
     * @return the partition
     */
    public List<String> getReplicaRoundPartition(String replicaId, long roundNumber) {
        List<List<String>> partition = getRoundPartition(roundNumber);

        for (List<String> replicaPartition : partition) {
            if (replicaPartition.contains(replicaId)) {
                return replicaPartition;
            }
        }

        throw new IllegalArgumentException("Replica ID not found in any partition: " + replicaId);
    }

    /**
     * Check if two nodes can communicate with each other, given a specific partition.
     *
     * @param sender    the sender node (internal ID of internal "twin" replica)
     * @param recipient the recipient node
     * @return true if the two nodes can communicate
     */
    public boolean canSendMessage(String sender, String recipient, MessagePayload message) {
        List<String> partition;
        if (message instanceof MessageWithRound messageWithRound) {
            // If the message has a round number, use the partition for that round
            partition = getReplicaRoundPartition(sender, messageWithRound.getRound());
        } else {
            // Default to the default partition
            partition = getDefaultPartitions()
                    .stream()
                    .filter(p -> p.contains(sender))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Sender not found in any partition: " + sender));
        }

        if (partition == null) {
            throw new IllegalArgumentException("Sender not found in any partition: " + sender);
        }

        return partition.contains(recipient);
    }

    @Override
    public synchronized void sendMessage(Node sender, MessagePayload message, String recipient) {
        // Sanity check
        if (!(sender instanceof Replica replicaSender)) {
            throw new IllegalArgumentException("Sender must be a Replica");
        }

        String internalId = this.twinsReplica.getInternalId(replicaSender);

        if (canSendMessage(internalId, recipient, message)) {
            this.getScenario().getTransport().sendMessage(sender, message, recipient);
        } else {
            log.info("Message from " + internalId + " to " + recipient + " blocked by partitioning");
        }
    }

    @Override
    public synchronized void multicast(Node sender, SortedSet<String> recipients, MessagePayload payload) {
        // Sanity check
        if (!(sender instanceof Replica replicaSender)) {
            throw new IllegalArgumentException("Sender must be a Replica");
        }

        String internalId = this.twinsReplica.getInternalId(replicaSender);

        // Send the message to the available recipients, if any
        List<String> availableRecipients = recipients.stream()
                .filter(recipient -> canSendMessage(internalId, recipient, payload))
                .toList();
        if (!availableRecipients.isEmpty()) {
            this.getScenario().getTransport().multicast(sender, new TreeSet<>(availableRecipients), payload);
        }

        // Log the dropped recipients, if any
        List<String> droppedRecipients = recipients.stream()
                .filter(recipient -> !canSendMessage(internalId, recipient, payload))
                .toList();
        if (!droppedRecipients.isEmpty()) {
            log.info("Message from " + internalId + " to " + droppedRecipients + " blocked by partitioning");
        }
    }

    @Override
    public synchronized void sendClientResponse(Node sender, MessagePayload response, String recipient) {
        // Sanity check
        if (!(sender instanceof Replica replicaSender)) {
            throw new IllegalArgumentException("Sender must be a Replica");
        }

        String internalId = this.twinsReplica.getInternalId(replicaSender);

        if (canSendMessage(internalId, recipient, response)) {
            this.getScenario().getTransport().sendMessage(sender, response, recipient);
        } else {
            log.info("Message from " + internalId + " to " + recipient + " blocked by partitioning");
        }
    }

    @Override
    public synchronized long setTimeout(Node node, Runnable runnable, Duration timeout) {
        Long eventId = this.getScenario().getTransport().setTimeout(node, runnable, timeout);

        this.queuedTimeouts
                .computeIfAbsent(node.getId(), k -> new ArrayList<>())
                .add(eventId);

        return eventId;
    }

    @Override
    public synchronized void clearTimeout(Node node, long eventId) {
        this.getScenario().getTransport().clearTimeout(node, eventId);

        this.queuedTimeouts
                .computeIfAbsent(node.getId(), k -> new ArrayList<>())
                .remove(eventId);
    }

    @Override
    public synchronized void clearReplicaTimeouts(Node node) {
        if (!(node instanceof Replica replica)) {
            throw new IllegalArgumentException("Node must be a Replica");
        }

        String internalId = this.twinsReplica.getInternalId(replica);

        List<Long> replicaTimeouts = this.queuedTimeouts.getOrDefault(internalId, Collections.emptyList());

        replicaTimeouts.forEach(eventId -> this.getScenario().getTransport().clearTimeout(node, eventId));
        replicaTimeouts.clear();
    }
}
