package byzzbench.simulator.exploration_strategy.twins;

import byzzbench.simulator.exploration_strategy.byzzfuzz.MessageWithByzzFuzzRoundInfo;
import byzzbench.simulator.nodes.Replica;
import byzzbench.simulator.state.TotalOrderCommitLog;
import byzzbench.simulator.transport.MessagePayload;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A Twins {@link Replica} that emulates byzantine behavior
 * by switching between different replica instances behind the scenes.
 * <p>
 * Every instance shares the same external identity ({@link #getId()}); to the
 * rest of the system the twins are indistinguishable from a single node. The
 * per-round network partition that decides which instance(s) see a given message
 * is a <b>global</b> schedule shared by all nodes, held by the
 * {@link TwinsTransport} injected via {@link #setTwinsTransport(TwinsTransport)}.
 * <p>
 * See "Twins: BFT Systems Made Robust" by Shehar Bano, Alberto Sonnino,
 * Andrey Chursin, Dmitri Perelman, Zekun Li, Avery Ching and Dahlia Malkhi.
 * https://drops.dagstuhl.de/entities/document/10.4230/LIPIcs.OPODIS.2021.7
 */
@Getter
@Log
public class TwinsReplica extends Replica {
    /**
     * The shared, global Twins transport holding the per-round partitions.
     * Injected by the {@link TwinsExplorationStrategy} once all twins exist.
     */
    @Setter
    private TwinsTransport twinsTransport;

    /**
     * The list of replica instances that are part of this Twins replica.
     */
    private final ArrayList<Replica> replicas = new ArrayList<>();

    /**
     * Create a new Twins replica.
     *
     * @param replica  the replica to clone
     * @param numTwins the number of twin instances to create (>= 2)
     */
    public TwinsReplica(Replica replica, int numTwins) {
        super(replica.getId(), replica.getScenario(), new TotalOrderCommitLog());

        // Sanity check: must have at least 2 twins
        if (numTwins < 2) {
            throw new IllegalArgumentException("numTwins must be at least 2");
        }

        // Sanity check: cannot create Twins replica from another Twins replica
        if (replica instanceof TwinsReplica) {
            throw new IllegalArgumentException("Cannot create Twins replica from another Twins replica");
        }

        // create the twin copies (all sharing the same external identity)
        replicas.add(replica);
        for (int i = 1; i < numTwins; i++) {
            Replica twin = replica.getScenario().cloneReplica(replica);
            replicas.add(twin);
            twin.initialize();
        }

        // Mark ourselves as faulty
        this.markFaulty();
    }

    /**
     * Get the internal "virtual" replica IDs.
     *
     * @return the internal IDs
     */
    protected List<String> getInternalIds() {
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < replicas.size(); i++) {
            ids.add(String.format("%s:%d", getId(), i));
        }
        return ids;
    }

    /**
     * Get the internal ID for the given replica.
     *
     * @param replica the replica to get the ID for
     * @return the internal ID for the internal "twin" replica
     */
    protected String getInternalId(Replica replica) {
        return String.format("%s:%d", getId(), replicas.indexOf(replica));
    }

    /**
     * Check if the given ID is an internal ID.
     *
     * @param id the ID to check
     * @return true if the ID is an internal ID
     */
    private boolean isInternalId(String id) {
        return id.startsWith(getId() + ":");
    }

    /**
     * Get the Replica object corresponding to the given ID.
     *
     * @param id the ID of the replica
     * @return the replica object
     */
    private Replica getInternalReplicaFromId(String id) {
        if (!isInternalId(id)) {
            throw new IllegalArgumentException("Not an internal ID: " + id);
        }
        int index = Integer.parseInt(id.substring(getId().length() + 1));
        return replicas.get(index);
    }

    /**
     * Get the internal replicas that should handle the given message, according
     * to the global per-round partition: an instance handles the message only if
     * it shares the sender's partition for that round.
     *
     * @param sender  the (external) id of the sender of the message
     * @param message the message to deliver
     * @return the list of internal replicas that should handle the message
     */
    public List<Replica> getInternalReplicasHandlingMessage(String sender, MessagePayload message) {
        // Messages without round info (e.g. client requests) are not partitioned:
        // deliver to all twin instances.
        if (!(message instanceof MessageWithByzzFuzzRoundInfo roundInfo)) {
            return new ArrayList<>(replicas);
        }

        long round = roundInfo.getRound();
        List<String> senderAddresses = this.twinsTransport.getEffectiveAddresses(sender);

        // Collect the addresses reachable from the sender in this round's partition.
        Set<String> reachable = new HashSet<>();
        boolean senderPartitioned = false;
        for (String senderAddress : senderAddresses) {
            List<String> partition = this.twinsTransport.getPartitionContaining(senderAddress, round);
            if (partition != null) {
                senderPartitioned = true;
                reachable.addAll(partition);
            }
        }

        // Sender outside the partition universe (e.g. a client): deliver to all.
        if (!senderPartitioned) {
            return new ArrayList<>(replicas);
        }

        // Deliver only to this twin's instances that share a partition with the sender.
        return getInternalIds().stream()
                .filter(reachable::contains)
                .map(this::getInternalReplicaFromId)
                .toList();
    }

    @Override
    public void handleMessage(String sender, MessagePayload message) {
        List<Replica> internalReplicas = this.getInternalReplicasHandlingMessage(sender, message);

        // deliver to all sub-replicas in the sender's partition
        for (Replica internalReplica : internalReplicas) {
            internalReplica.handleMessage(sender, message);
        }
    }
}
