package byzzbench.simulator.exploration_strategy.twins;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.exploration_strategy.byzzfuzz.MessageWithByzzFuzzRoundInfo;
import byzzbench.simulator.nodes.Node;
import byzzbench.simulator.nodes.Replica;
import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.transport.Transport;
import lombok.Getter;
import lombok.extern.java.Log;

import java.io.Serializable;
import java.time.Duration;
import java.util.*;

/**
 * A transport layer that enforces a <b>single, global, per-round network
 * partition</b> shared by every replica in the scenario, as described in
 * "Twins: BFT Systems Made Robust" by Shehar Bano, Alberto Sonnino, Andrey
 * Chursin, Dmitri Perelman, Zekun Li, Avery Ching and Dahlia Malkhi.
 * https://drops.dagstuhl.de/entities/document/10.4230/LIPIcs.OPODIS.2021.7
 * <p>
 * The partitions are a property of the <i>scenario</i>, not of an individual
 * twin: a message between two nodes is delivered for a given round only if both
 * the (effective) sender address and the (effective) recipient address belong
 * to the same partition of that round. The same schedule governs honest&harr;honest,
 * honest&harr;twin and twin&harr;honest links alike.
 * <p>
 * Twin instances are represented in the partition by their internal addresses
 * ({@code <id>:0}, {@code <id>:1}, ...) while honest replicas are represented by
 * their plain id. This lets a node and its twin be placed in different
 * partitions (account-address filtering, not public-key filtering), which is
 * what enables equivocation/amnesia/lost-state behaviors.
 */
@Log
public class TwinsTransport extends Transport implements Serializable {
    /**
     * The single global per-round partition schedule shared by all nodes.
     * Maps a round number to the list of partitions in effect for that round.
     */
    @Getter
    private final NavigableMap<Long, List<List<String>>> roundPartitions = new TreeMap<>();

    /**
     * Partition to use when a message carries no round information: a single
     * partition containing the whole universe (i.e. fully connected).
     */
    @Getter
    private final List<List<String>> defaultPartitions;

    /**
     * Effective (partition-universe) address of each replica instance. Honest
     * replicas map to their id; twin instances map to their internal id.
     */
    private final Map<Replica, String> senderAddress = new HashMap<>();

    /**
     * Maps an external recipient id to the effective addresses it expands to. An
     * honest replica maps to a singleton of its id; a twin replica maps to the
     * internal ids of all its instances.
     */
    private final Map<String, List<String>> recipientAddresses = new HashMap<>();

    /**
     * Ids of the consensus replicas. Used to let client traffic bypass
     * inter-replica partitioning.
     */
    private final Set<String> replicaIds = new HashSet<>();

    /**
     * Queued timeout event ids, tracked per effective address so that a twin
     * instance's timeouts can be cleared independently of its sibling (which
     * shares the same external id).
     */
    @Getter
    private final Map<String, List<Long>> queuedTimeouts = new HashMap<>();

    /**
     * Create the global Twins transport.
     *
     * @param scenario        the scenario
     * @param roundPartitions the single, global per-round partition schedule
     * @param universe        the partition universe (all effective addresses)
     */
    public TwinsTransport(Scenario scenario, Map<Long, List<List<String>>> roundPartitions, List<String> universe) {
        super(scenario);
        this.roundPartitions.putAll(roundPartitions);
        this.defaultPartitions = List.of(new ArrayList<>(universe));

        // Build the address book for every consensus replica in the scenario.
        for (Replica replica : scenario.getReplicas().values()) {
            this.replicaIds.add(replica.getId());
            if (replica instanceof TwinsReplica twin) {
                this.recipientAddresses.put(twin.getId(), twin.getInternalIds());
                for (Replica instance : twin.getReplicas()) {
                    this.senderAddress.put(instance, twin.getInternalId(instance));
                }
            } else {
                this.recipientAddresses.put(replica.getId(), List.of(replica.getId()));
                this.senderAddress.put(replica, replica.getId());
            }
        }
    }

    /**
     * Get the partitions in effect for the given round (fully connected if the
     * round has no scheduled partition, e.g. when over-running past numRounds).
     *
     * @param roundNumber the round number
     * @return the partitions to use
     */
    public List<List<String>> getRoundPartition(long roundNumber) {
        return this.roundPartitions.getOrDefault(roundNumber, this.defaultPartitions);
    }

    /**
     * Get the effective partition-universe addresses an external id expands to.
     *
     * @param externalId the external (network-visible) id
     * @return the effective addresses
     */
    public List<String> getEffectiveAddresses(String externalId) {
        return this.recipientAddresses.getOrDefault(externalId, List.of(externalId));
    }

    /**
     * Get the partition (for the given round) that contains the given address,
     * or {@code null} if the address is not part of any partition.
     *
     * @param address the effective address
     * @param round   the round number
     * @return the partition containing the address, or null
     */
    public List<String> getPartitionContaining(String address, long round) {
        for (List<String> partition : getRoundPartition(round)) {
            if (partition.contains(address)) {
                return partition;
            }
        }
        return null;
    }

    /**
     * Get the effective partition-universe address of a sending node.
     */
    private String getEffectiveAddress(Node node) {
        if (node instanceof Replica replica) {
            return this.senderAddress.getOrDefault(replica, replica.getId());
        }
        return node.getId();
    }

    /**
     * Decide whether a message from {@code sender} can reach {@code recipient}
     * under the global partition for the message's round.
     *
     * @param sender    the sending node
     * @param recipient the external recipient id
     * @param message   the message being sent
     * @return true if the message can be delivered
     */
    public boolean canSendMessage(Node sender, String recipient, MessagePayload message) {
        // Client (or otherwise non-consensus) traffic is never partitioned.
        if (!this.replicaIds.contains(recipient)) {
            return true;
        }

        // Without round information we cannot place the message in a round, so it
        // is not subject to round-based partitioning (treated as fully connected).
        if (!(message instanceof MessageWithByzzFuzzRoundInfo roundInfo)) {
            return true;
        }

        long round = roundInfo.getRound();
        String senderAddr = getEffectiveAddress(sender);
        List<String> senderPartition = getPartitionContaining(senderAddr, round);

        // Sender outside the partition universe: fail open.
        if (senderPartition == null) {
            return true;
        }

        // Deliverable iff some instance of the recipient shares the partition.
        return getEffectiveAddresses(recipient).stream().anyMatch(senderPartition::contains);
    }

    @Override
    public synchronized void sendMessage(Node sender, MessagePayload message, String recipient) {
        if (canSendMessage(sender, recipient, message)) {
            this.getScenario().getTransport().sendMessage(sender, message, recipient);
        } else {
            log.info("Message from " + getEffectiveAddress(sender) + " to " + recipient + " blocked by partitioning");
        }
    }

    @Override
    public synchronized void multicast(Node sender, SortedSet<String> recipients, MessagePayload payload) {
        SortedSet<String> allowed = new TreeSet<>();
        SortedSet<String> dropped = new TreeSet<>();
        for (String recipient : recipients) {
            if (canSendMessage(sender, recipient, payload)) {
                allowed.add(recipient);
            } else {
                dropped.add(recipient);
            }
        }

        if (!allowed.isEmpty()) {
            this.getScenario().getTransport().multicast(sender, allowed, payload);
        }
        if (!dropped.isEmpty()) {
            log.info("Message from " + getEffectiveAddress(sender) + " to " + dropped + " blocked by partitioning");
        }
    }

    @Override
    public synchronized void sendClientResponse(Node sender, MessagePayload response, String recipient) {
        // Client responses are not subject to inter-replica partitioning.
        this.getScenario().getTransport().sendMessage(sender, response, recipient);
    }

    @Override
    public synchronized long setTimeout(Node node, Runnable runnable, Duration timeout, String description) {
        long eventId = this.getScenario().getTransport().setTimeout(node, runnable, timeout, description);
        this.queuedTimeouts
                .computeIfAbsent(getEffectiveAddress(node), k -> new ArrayList<>())
                .add(eventId);
        return eventId;
    }

    @Override
    public synchronized void clearTimeout(Node node, long eventId) {
        this.getScenario().getTransport().clearTimeout(node, eventId);
        this.queuedTimeouts
                .getOrDefault(getEffectiveAddress(node), Collections.emptyList())
                .remove(Long.valueOf(eventId));
    }

    @Override
    public synchronized void clearNodeTimeouts(Node node) {
        String address = getEffectiveAddress(node);
        List<Long> timeouts = this.queuedTimeouts.getOrDefault(address, Collections.emptyList());

        for (Long eventId : new ArrayList<>(timeouts)) {
            try {
                this.getScenario().getTransport().clearTimeout(node, eventId);
            } catch (IllegalArgumentException e) {
                // Timeout already fired or cleared; nothing to do.
            }
        }
        timeouts.clear();
    }
}
