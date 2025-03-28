package byzzbench.simulator.scheduler.twins;

import byzzbench.simulator.Replica;
import byzzbench.simulator.state.TotalOrderCommitLog;
import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.transport.messages.MessageWithRound;
import byzzbench.simulator.utils.StirlingNumberSecondKind;
import lombok.Getter;
import lombok.extern.java.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A Twins {@link Replica} that emulates byzantine behavior
 * by switching between different replica instances behind the scenes.
 * <p>
 * See "Twins: BFT Systems Made Robust" by Shehar Bano, Alberto Sonnino,
 * Andrey Chursin, Dmitri Perelman, Zekun Li, Avery Ching and Dahlia Malkhi.
 * https://drops.dagstuhl.de/entities/document/10.4230/LIPIcs.OPODIS.2021.7
 */
@Getter
@Log
public class TwinsReplica extends Replica {
    private final TwinsTransport twinsTransport;
    /**
     * The list of replicas that are part of this Twins replica.
     */
    private final ArrayList<Replica> replicas = new ArrayList<>();
    Random rand = new Random();

    /**
     * Create a new Twins replica.
     *
     * @param replica   the replica to clone
     * @param numTwins  the number of twins to create
     * @param numRounds the number of rounds to generate partitions for
     */
    public TwinsReplica(Replica replica, int numTwins, int numRounds) {
        super(replica.getId(), replica.getScenario(), new TotalOrderCommitLog());

        // Sanity check: must have at least 2 twins
        if (numTwins < 2) {
            throw new IllegalArgumentException("numTwins must be at least 2");
        }

        // Sanity check: cannot create Twins replica from another Twins replica
        if (replica instanceof TwinsReplica) {
            throw new IllegalArgumentException("Cannot create Twins replica from another Twins replica");
        }

        // create the twin copies
        replicas.add(replica);
        for (int i = 1; i < numTwins; i++) {
            Replica twin = replica.getScenario().cloneReplica(replica);
            replicas.add(twin);
            twin.initialize();
        }

        // Update the replicas to use a Twins transport
        // Note: this must be done after the replicas are created and added to the list!
        this.twinsTransport = new TwinsTransport(this);
        this.replicas.forEach(r -> r.setTransport(this.twinsTransport));

        // Create some round partitions.
        List<String> nodeIds = new ArrayList<>(replica.getNodeIds().stream().filter(id -> !id.equals(this.getId())).toList());
        nodeIds.addAll(getInternalIds());
        List<List<List<String>>> options = StirlingNumberSecondKind.getPartitions(nodeIds, 1);
        options.addAll(StirlingNumberSecondKind.getPartitions(nodeIds, 2));
        options.addAll(StirlingNumberSecondKind.getPartitions(nodeIds, 3));
        for (long i = 0; i < numRounds; i++) {
            this.twinsTransport.getRoundPartitions().put(i, options.get(rand.nextInt(options.size())));
        }

        // print getRoundPartitions, one per line
        this.twinsTransport.getRoundPartitions().forEach((k, v) -> log.info("Round " + k + ": " + v));

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
     * Get the internal replicas that should handle the given message.
     *
     * @param sender  the sender of the message
     * @param message the message to deliver
     * @return the list of internal replicas that should handle the message
     */
    public List<Replica> getInternalReplicasHandlingMessage(String sender, MessagePayload message) {
        List<String> partition;
        if (message instanceof MessageWithRound messageWithRound) {
            // If the message has a round number, use the partition for that round
            partition = this.twinsTransport.getReplicaRoundPartition(sender, messageWithRound.getRound());
        } else {
            // Default to the default partition
            // If the sender is not in *any* partition, default to delivering to all internal replicas
            partition = this.twinsTransport.getDefaultPartitions()
                    .stream()
                    .filter(p -> p.contains(sender))
                    .findFirst()
                    .orElse(this.getInternalIds()); // default to all internal replicas handling the message
        }

        if (partition == null) {
            throw new IllegalArgumentException("Sender not found in any partition: " + sender);
        }

        return partition.stream()
                .filter(this::isInternalId)
                .map(this::getInternalReplicaFromId)
                .toList();
    }

    @Override
    public void handleMessage(String sender, MessagePayload message) {
        List<Replica> internalReplicas = this.getInternalReplicasHandlingMessage(sender, message);

        // deliver to all sub-replicas
        for (Replica internalReplica : internalReplicas) {
            internalReplica.handleMessage(sender, message);
        }
    }
}
