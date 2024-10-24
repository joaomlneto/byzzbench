package byzzbench.simulator.protocols.pbft;

import byzzbench.simulator.protocols.pbft.message.DataMessage;
import byzzbench.simulator.protocols.pbft.message.FetchMessage;
import byzzbench.simulator.protocols.pbft.message.MetadataDigestMessage;
import byzzbench.simulator.protocols.pbft.message.MetadataMessage;

import java.time.Instant;
import java.util.*;

/**
 * Handles state digesting and checkpointing.
 * <p>
 * The state consists of several objects, the abstraction function is "gets" and "puts"
 * is one of its inverses. The procedures invoked before and after recovery to
 * save and restore extra state information are "shutdown_p" and "restart_p".
 */
public class State {
    /**
     * Actual memory holding current state and the number of Blocks in that memory
     * FIXME: Just in BFT mode
     */
    private final List<String> mem = new ArrayList<>();
    /**
     * Actual memory holding current state and the number of Blocks in that memory.
     * FIXME: Just in BASE mode
     */
    private final List<String> rep_mem = new ArrayList<>();
    /**
     * Parent replica object.
     */
    private final PbftReplica replica;
    /**
     * Bitmap with a bit for each block in the memory region indicating whether the block should be copied the next time
     * it is written; blocks should be copied iff their bit is 0.
     */
    private final BitSet cowb;
    /**
     * Number of blocks???
     */
    private int nb;

    /**
     * Partition tree
     */
    private List<Part> ptree;

    /**
     * Tree of sums of digests of subpartitions.
     */
    private List<DSum> stree;

    /**
     * Checkpoint log
     */
    private List<CheckpointRec> clog;

    /**
     * Sequence number of the last checkpoint
     */
    private long lc;

    /**
     * True iff replica is fetching missing state
     */
    private boolean fetching;

    /**
     * Whether to keep last checkpoints
     */
    private boolean keep_ckpts;

    /**
     * Level of state partition info being fetched
     */
    private int flevel;

    /**
     * Queue of out-of-date partitions for each level
     */
    private Object stalep;

    /**
     * Set of fetched page sin a fetch operation
     * XXX: #ifndef NO_STATE_TRANSLATION
     */
    private Object fetched_pages;

    /**
     * Copies of pages at last checkpoint
     * XXX: #ifndef NO_STATE_TRANSLATION
     */
    private Object pages_lc;

    /**
     * Certificate for partition we are working on
     */
    private Certificate<MetadataMessage> cert;

    /**
     * ID of last replica we chose as replier
     */
    private String lreplier;

    /**
     * Time when last fetch was sent
     */
    private Instant last_fetch_t;

    /**
     * Is replica in checking state
     */
    private boolean checking;

    /**
     * Last checkpoint sequence number when checking started
     */
    private long check_start;

    /**
     * If replica state is known to be corrupt
     */
    private boolean corrupt;

    /**
     * Check for messages after digesting this many blocks
     */
    private int poll_cnt;

    /**
     * Queue of partitions whose digests need to be checked.
     * It can have partitions from different levels.
     */
    private Queue<Object> to_check;

    /**
     * Index of last block checked in to_check.high()
     */
    private int lchecked;

    /**
     * Level of ancestor of current partition whose subpartitions
     * have already been added to to_check.
     */
    private int refetch_level;

    /**
     * Total size of object being fetched.
     * XXX: #ifndef NO_STATE_TRANSLATION
     */
    private int total_size;

    /**
     * Next fragment number to be fetched.
     * XXX: #ifndef NO_STATE_TRANSLATION
     */
    private int next_chunk;

    /**
     * Buffer for reassembling the fragments.
     * XXX: #ifndef NO_STATE_TRANSLATION
     */
    private String reassemb;

    /**
     * Creates a new state object for replica "replica"
     *
     * @param replica the parent replica object
     */
    public State(PbftReplica replica) {
        this.replica = replica;

        // TODO: Specify number of bits
        this.cowb = new BitSet();
    }

    /**
     * Computes a state digest from scratch and a digest for each partition
     */
    public void compute_full_digest() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * If there is a checkpoint for sequence number "n" in this, returns true
     * and returns its digest. Otherwise, returns nothing.
     *
     * @param n sequence number of checkpoint
     * @return digest of checkpoint
     */
    public Optional<Digest> digest(long n) {
        // XXX: the signature is slightly modified from the C implementation
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Sets "d" to the current digest of partition "(l,i)"
     *
     * @param d digest to set
     * @param l level
     * @param i index
     * @return size of object in partition (l,i)
     */
    private int digest(Digest d, int l, int i) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Sets "d" to MD5(i#lm#(data,size))
     *
     * @param d    digest to set
     * @param i    index
     * @param lm   sequence number of last checkpoint that modified partition
     * @param data data to digest
     * @param size size of data
     */
    private void digest(Digest d, int i, long lm, String data, int size) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Checks if the digest of the partition in "m" is "d"
     *
     * @param d digest to check
     * @param m metadata message
     * @return true if the digest of the partition in "m" is "d", otherwise false
     */
    private boolean check_digest(Digest d, MetadataMessage m) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * It decrements flevel and, if parent is consistent, removes parent.
     * If the queue of parent becomes empty it calls itself recursively.
     * Unless there is no parent, in which case it sets in_fetch_state_to
     * false and updates state accordingly.
     * Requires flevel has an empty out-of-date queue.
     */
    private void done_with_level() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Updates the digests of the blocks whose cow bits were reset since the last
     * checkpoint and computes a new state digest using the state digest computed
     * during the last checkpoint.
     *
     * @param n sequence number of last checkpoint
     */
    private void update_ptree(long n) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * If there is a checkpoint with sequence number "c" in this, returns
     * the data for block index "i" at checkpoint "c".
     *
     * @param c sequence number of checkpoint
     * @param i index of block
     * @return data for block index "i" at checkpoint "c"
     */
    private String get_data(long c, int i) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Returns a pointer to the information for partition "(l,i)" at checkpoint "c".
     * Requires there is a checkpoint with sequence number "c" in this.
     *
     * @param c sequence number of checkpoint
     * @param l level
     * @param i index
     * @return information for partition "(l,i)" at checkpoint "c"
     */
    private String get_meta_data(long c, int l, int i) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Checks whether the actual digest of block "i" and its digest in the ptree match.
     *
     * @param i index of block
     * @return true if the actual digest of block "i" and its digest in the ptree match, otherwise false
     */
    private boolean check_data(int i) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Check if replica is fetching missing state
     *
     * @return true if replica is fetching missing state, otherwise false
     */
    public boolean in_fetch_state() {
        return this.fetching;
    }

    /**
     * Sends fetch message for missing state. If "c != -1" then
     * "cd" points to the digest of checkpoint sequence number "c". "stable"
     * should be true iff the specific checkpoint being fetched is stable.
     *
     * @param last_exec sequence number of last executed request
     * @param c         sequence number of checkpoint
     * @param cd        digest of checkpoint
     * @param stable    true if checkpoint is stable, otherwise false
     */
    public void start_fetch(long last_exec, long c, Digest cd, boolean stable) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Sends fetch message for missing state.
     *
     * @param last_exec sequence number of last executed request
     */
    public void start_fetch(long last_exec) {
        start_fetch(last_exec, -1, null, false);
    }

    /**
     * Sends fetch message requesting missing state.
     *
     * @param change_replier true if replier should be changed, otherwise false
     */
    public void send_fetch(boolean change_replier) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Check if replica is checking state
     *
     * @return true if replica is checking state, otherwise false
     */
    public boolean in_check_state() {
        return this.checking;
    }

    /**
     * Starts checking state that reflects execution up to "last_exec"
     *
     * @param last_exec sequence number of last executed request
     */
    public void start_check(long last_exec) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Checks if state is correct
     */
    public void check_state() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Shuts down state writing value to "o"
     *
     * @return true if successful, otherwise false
     */
    public boolean shutdown(String o, long ls) {
        // FIXME: the signature is modified from the C implementation.
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Restarts the state object from value in "i"
     *
     * @param i       state to restart from
     * @param rep     replica to restart
     * @param ls      sequence number of last stable checkpoint ???
     * @param le      sequence number of last executed request ???
     * @param corrupt true if state is corrupt, otherwise false
     * @return true if successful, otherwise false
     */
    public boolean restart(String i, PbftReplica rep, long ls, long le, boolean corrupt) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Enforces that there is no information about bound "b". "ks" is the
     * maximum sequence number that I know is stable.
     *
     * @param b       bound to enforce
     * @param ks      maximum sequence number that is stable
     * @param corrupt true if state is corrupt, otherwise false?
     * @return true if successful, otherwise false
     */
    public boolean enforce_bound(long b, long ks, boolean corrupt) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Handle {@MetadataMessage} message
     *
     * @param m metadata message
     */
    public void handle(MetadataMessage m) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Handle {@MetadataDigestMessage} message
     *
     * @param m metadata digest message
     */
    public void handle(MetadataDigestMessage m) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Handle {@DataMessage} message
     *
     * @param m data message
     */
    public void handle(DataMessage m) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Check if message was able to be verified
     *
     * @param m message to check
     * @return true if message was able to be verified, otherwise false
     */
    public boolean handle(FetchMessage m, long last_stable) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Discards incomplete certificate
     */
    public void mark_stale() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Simulates a reboot by invalidating state
     */
    public void simulate_reboot() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Returns true iff fetch should be retransmitted
     *
     * @param cur current time
     * @return true if fetch should be retransmitted, otherwise false
     */
    public boolean retrans_fetch(Instant cur) {
        // FIXME: Magic number
        return fetching && cur.isAfter(last_fetch_t.plusMillis(100));
    }

    /**
     * Copies block with bindex and marks it as copied
     *
     * @param bindex index of block to copy
     */
    public void cow_single(int bindex) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Performs copies for the blocks in mem if they
     * have not been copied since last checkpoint.
     * It also marks them as copied.
     *
     * @param mem memory to copy
     */
    public void cow(String[] mem) {
        // argument should be a list of blocks!!
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Saves a checkpoint of the current state (associated with seqno)
     * and computes the digest of all partitions.
     *
     * @param seqno sequence number of checkpoint
     */
    public void checkpoint(long seqno) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Rolls back to the last checkpoint and returns its sequence number.
     * Requires !in_fetch_state and there is a checkpoint in this
     *
     * @return sequence number of last checkpoint
     */
    public long rollback() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Discards the checkpoints with sequence number less than or equal to
     * "seqno" and saves information about the current state whose sequence
     * number is "le"
     *
     * @param seqno sequence number of oldest checkpoint to keep
     * @param le    sequence number of current state
     */
    public void discard_checkpoint(long seqno, long le) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Checkpoint Record
     */
    public class CheckpointRec {
        /**
         * Map for partitions that were modified since this checkpoint was taken and
         * therefore the next checkpoint.
         * FIXME: The key type may be wrong!!
         */
        private final SortedMap<String, Part> parts = new TreeMap<>();

        /**
         * Deletes all parts in record and removes them
         */
        public void clear() {
            throw new UnsupportedOperationException("Not implemented");
        }

        /**
         * Appends partition index "i" at level "l" with value "p" to the record.
         * Requires that fetch(l, i) == 0.
         *
         * @param l level
         * @param i index
         * @param p partition
         */
        public void append(int l, int i, Part p) {
            throw new UnsupportedOperationException("Not implemented");
        }

        /**
         * Like append() but without the requires clause: appends partition
         * index "i" at level "l" with value "p" to the record.
         *
         * @param l level
         * @param i index
         * @param p partition
         */
        public void appendr(int l, int i, Part p) {
            throw new UnsupportedOperationException("Not implemented");
        }

        /**
         * If there is a partition with index "i" from level "l" in this,
         * returns a pointer to its information. Otherwise, returns null.
         *
         * @param l level
         * @param i index
         * @return partition
         */
        public Optional<Part> fetch(int l, int i) {
            throw new UnsupportedOperationException("Not implemented");
        }

        /**
         * Returns the number of entries in the record
         *
         * @return number of entries
         */
        public int num_entries() {
            throw new UnsupportedOperationException("Not implemented");
        }
    }

    /**
     * Blocks are grouped into partitions that form a hierarchy.
     * Part contains information about one such partition.
     */
    public class Part {
        /**
         * Sequence number of last checkpoint that modified partition
         */
        long lm = 0;

        /**
         * Digest of partition
         */
        Digest d;

        /**
         * Size of object for level 'PLevels-1'
         */
        int size;
    }

    /**
     * Information about stale partitions being fetched.
     */
    public class FPart {
        /**
         * Index of partition???
         */
        int index;
        /**
         * Latest checkpoint seqno for which partition is up-to-date
         */
        long lu;
        /**
         * Sequence number of last checkpoint that modified partition
         */
        long lm;
        /**
         * Sequence number of checkpoint being fetched
         */
        long c;
        /**
         * Digest of checkpoint being fetched
         */
        Digest d;
        /**
         * Size of leaf object
         */
        int size;
    }

    /**
     * Information about partitions whose digest is being checked
     */
    public class CPart {
        int index;
        int level;
    }

    public class DSum {
        static final int nbits = 256;
        static final int mp_limb_bits = 8; // * sizeof mp_limb_t
        static final int nlimbs = (nbits + mp_limb_bits - 1) / mp_limb_bits;
        //static final int nbytes = nlimbs; // * sizeof(mp_limb_t);
        static DSum M; // Modulus for sums must be at most nbits-1 long.

        /**
         * FIXME: This is a placeholder for the actual implementation!
         */
        public Digest[] mp_limb_t;

        /**
         * Create a new sum object with value 0
         */
        public DSum() {
            // create nlimbs limbs
            mp_limb_t = new Digest[nlimbs];
        }

        public DSum(DSum other) {
            mp_limb_t = new Digest[nlimbs];
            this.copyEntriesFrom(other);
        }

        public void copyEntriesFrom(DSum other) {
            System.arraycopy(other.mp_limb_t, 0, mp_limb_t, 0, nlimbs);
        }

        /**
         * Adds "d" to this
         *
         * @param d the digest to add
         */
        public void add(Digest d) {
            throw new UnsupportedOperationException("Not implemented");
        }

        /**
         * Subtracts "d" from this
         *
         * @param d the digest to subtract
         */
        public void sub(Digest d) {
            throw new UnsupportedOperationException("Not implemented");
        }
    }
}
