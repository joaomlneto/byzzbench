package byzzbench.simulator.protocols.pbft;

import byzzbench.simulator.protocols.pbft.message.MetadataDigestMessage;
import lombok.Data;

import java.util.*;

/**
 * A set of "matching" MetaDataD messages from different replicas
 */
public class MetaDataCertificate {
    /**
     * The parent replica object
     */
    private final PbftReplica replica;
    /**
     * Last last_stables in messages sent by each replica
     */
    private final SortedMap<String, Long> last_stables = new TreeMap<>();
    /**
     * Map with the last messages sent by each replica
     */
    private final SortedMap<String, List<MetadataDigestMessage>> last_mdds = new TreeMap<>();
    /**
     * Vector with all distinct part values in this
     */
    private final List<PartVal> vals = new LinkedList<>();
    /**
     * Maximum number of elements in vals
     */
    private final int max_size;
    /**
     * Value is correct if it appears in at least "correct" messages
     */
    private final int correct;
    /**
     * If c >= 0, the digest of partition "d" is up-to-date at sequence number "c"
     */
    private long c;
    /**
     * True iff replica's message is in this
     */
    private boolean has_my_message;
    /**
     * Last last_stable in this??
     */
    private long ls;
    /**
     * The digest value
     */
    private Digest d;

    /**
     * Creates an empty metadata certificate.
     */
    public MetaDataCertificate(PbftReplica replica) {
        this.replica = replica;

        for (String id : replica.getNodeIds()) {
            last_mdds.put(id, new LinkedList<>());
            last_stables.put(id, 0L);
        }
        this.ls = 0;

        this.max_size = replica.n() * (replica.getConfig().getMAX_OUT() / replica.getConfig().getCHECKPOINT_INTERVAL() + 1);
        //TUDO: initialize this.vals
        this.correct = replica.f() + 1;
        this.c = -1;
        this.has_my_message = false;
    }

    /**
     * Adds "m" to the MetaDataCertificate and returns true provided "m" satisfies:
     * 1. there is no message from "m->id" in the this or, if there is such a message
     * "m1", "m1->last_checkpoint() < m->last_checkpoint()"
     * 2. "m->verify() == true"
     * Otherwise, it has no effect on this and returns false.
     *
     * @param m    the message to add
     * @param mine true if the message is from the calling principal
     * @return true if the message was added, false otherwise
     */
    public boolean add(MetadataDigestMessage m, boolean mine) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Returns true iff my message is in this
     *
     * @return true iff my message is in this
     */
    public boolean has_mine() {
        return has_my_message;
    }

    /**
     * Check if a correct digest value was found. If so, returns the digest value
     * and the sequence number of the checkpont for which that value is known
     * to be up-to-date. Otherwise, returns false.
     *
     * @return the digest value and the sequence number of the checkpont, or nothing
     */
    public Optional<SequenceNumberAndDigest> cvalue() {
        if (c < 0) {
            return Optional.empty();
        }
        return Optional.of(new SequenceNumberAndDigest(c, d));
    }

    /**
     * Returns the greatest sequence number known to be stable
     *
     * @return the greatest sequence number known to be stable
     */
    public long last_stable() {
        return ls;
    }

    /**
     * Discards all messages in this and makes it empty
     */
    public void clear() {
        for (String id : replica.getNodeIds()) {
            last_mdds.get(id).clear();
            last_stables.put(id, 0L);
        }
        this.ls = 0;

        vals.clear();

        this.c = -1;
        this.has_my_message = false;
    }

    /**
     * A sequence number and a digest
     *
     * @param sequenceNumber the sequence number
     * @param digest         the digest
     */
    public record SequenceNumberAndDigest(long sequenceNumber, Digest digest) {
    }

    /**
     * Part value
     */
    @Data
    public class PartVal implements SeqNumLog.SeqNumLogEntry {
        private Digest d;
        private long c;
        private int count;

        public PartVal() {
            clear();
        }

        public void clear() {
            c = -1;
            count = 0;
        }
    }
}
