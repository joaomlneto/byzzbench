package byzzbench.simulator.protocols.pbft;

import java.time.Instant;
import java.util.*;

/**
 * A Certificate is a set of "matching" messages from different replicas.
 * The implementation assumes "correct > 0" and "complete > correct".
 */
public class Certificate<T extends CertifiableMessage> implements SeqNumLog.SeqNumLogEntry {
    /**
     * The parent replica object.
     */
    protected final PbftReplica replica;

    /**
     * Certificate is complete if "num_correct() >= complete"
     */
    private final int complete;
    //private final SortedSet<CertifiableMessage> messages = new TreeSet<>();

    /**
     * Set of replica IDs whose message is in this certificate.
     */
    private final SortedSet<String> bmap = new TreeSet<>();

    /**
     * The distinct message values in this certificate.
     */
    private final List<MessageVal<T>> vals = new LinkedList<>();

    /**
     * Maximum number of elements in vals, f+1
     */
    private final int max_size;

    /**
     * value is correct if it appears in at least "correct" messages.
     */
    private final int correct;

    /**
     * Correct certificate value, if known.
     */
    private MessageVal<T> c = new MessageVal<>();

    /**
     * My message in this, or null if I have no message in this
     */
    private T mym;

    /**
     * Time at which mym was last sent
     */
    private Instant t_sent;

    /**
     * Creates an empty certificate. The certificate is complete when it contains at least "complete" matching messages
     * from different replicas. If the complete argument is omitted (or 0) it is taken to be 2f+1.
     *
     * @param comp "complete" >= f+1 or 0
     */
    public Certificate(PbftReplica replica, int comp) {
        this.replica = replica;
        this.max_size = replica.f() + 1;
        this.correct = replica.f() + 1;
        this.complete = comp == 0 ? replica.f() * 2 + 1 : comp;
    }

    /**
     * Creates an empty certificate. The certificate is complete when it contains at least 2f+1 matching messages
     * from different replicas.
     */
    public Certificate(PbftReplica replica) {
        this(replica, 0);
    }

    /**
     * Adds "m" to the certificate and returns true provided "m" satisfies:
     * 1. there is no message from "m.id()" in the certificate
     * 2. "m.verify() == true"
     * 3. if "cvalue() != 0", "cvalue().match(m)";
     * otherwise, it has no effect on this and returns false. This becomes the owner of "m".
     *
     * @param m the message to add
     * @return true if the message was added, false otherwise
     */
    public boolean add(T m) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * If "cvalue() != 0" and "!cvalue().match(m)", it has no effect and returns false.
     * Otherwise, adds "m" to the certificate and returns. This becomes the owner of "m".
     * Requires the identifier of the calling principal is "m.id()".
     *
     * @param m the message to add
     * @return true if the message was added, false otherwise
     */
    public boolean add_mine(T m) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Returns caller's message in certificate or 0 if there is no such message.
     *
     * @return the message in the certificate and the time at which it was sent
     */
    public MessageWithTime<T> mine() {
        return new MessageWithTime<>(Optional.ofNullable(mym), t_sent);
    }

    /**
     * Returns the correct message value for this certificate or 0 if this value is not known.
     * Note that the certificate retains ownership over the returned value (e.g., if clear or mark_stale are called
     * the value may be deleted.)
     *
     * @return the correct message value for this certificate or 0 if this value is not known
     */
    public T cvalue() {
        return c != null ? c.m : null;
    }

    /**
     * Returns the correct message value for this certificate or 0 if this value is not known.
     * If it returns the correct value, it removes the message from the certificate and clears the certificate
     * (that is the caller gains ownership over the returned value.)
     *
     * @return the correct message value for this certificate or 0 if this value is not known
     */
    public T cvalue_clear() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Returns the number of messages with the correct value in this.
     *
     * @return the number of messages with the correct value in this
     */
    public int num_correct() {
        return c != null ? c.count : 0;
    }

    /**
     * Returns true iff the certificate is complete.
     *
     * @return true iff the certificate is complete
     */
    public boolean is_complete() {
        return num_correct() >= complete;
    }

    /**
     * If cvalue() is not null, makes the certificate complete.
     */
    public void make_complete() {
        if (c != null) {
            c.count = complete;
        }
    }

    /**
     * Discards all messages in certificate except mine.
     */
    public void mark_stale() {
        if (!is_complete()) {
            int i = 0;
            int old_cur_size = vals.size();
            if (mym != null) {
                if (!(mym.equals(c.m))) {
                    throw new IllegalStateException("Broken invariant");
                }
                c.m = null;
                c.count = 0;
                c = vals.get(0); // FIXME: might be wrong?!?
                c.m = mym;
                c.count = 1;
                i = 1;
            } else {
                c = null;
            }
            //cur_size = i; // FIXME: this should be automatically updated by the List

            for (; i < old_cur_size; i++) {
                vals.get(i).clear();
            }
            bmap.clear();
        }
    }

    /**
     * Discards all messages in certificate.
     */
    public void clear() {
        for (MessageVal<T> val : vals) {
            val.clear();
        }
        bmap.clear();
        c = null;
        mym = null;
        t_sent = null;
    }

    /**
     * Returns true iff the certificate is empty.
     *
     * @return true iff the certificate is empty
     */
    public boolean is_empty() {
        return bmap.isEmpty();
    }

    /**
     * Encodes object state from stream
     *
     * @return true if the message was successfully encoded, false otherwise
     */
    public boolean encode() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Decodes object state from stream
     *
     * @return true if the message was successfully decoded, false otherwise
     */
    public boolean decode() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public class MessageVal<T extends CertifiableMessage> {
        public T m;
        public int count;

        public MessageVal() {
            m = null;
            count = 0;
        }

        public void clear() {
            m = null;
            count = 0;
        }
    }
}
