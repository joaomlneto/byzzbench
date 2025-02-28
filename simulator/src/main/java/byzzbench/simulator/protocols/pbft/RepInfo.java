package byzzbench.simulator.protocols.pbft;

import byzzbench.simulator.protocols.pbft.message.ReplyMessage;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.time.Instant;
import java.util.Queue;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Holds the last replies sent to each principal
 */
public class RepInfo implements Serializable {
    /**
     * MAX_REP_SIZE
     */
    private static final int MAX_REP_SIZE = 8192;
    /**
     * The parent replica object
     */
    private final PbftReplica replica;
    /**
     * ireps
     */
    private final SortedMap<String, Rinfo> ireps = new TreeMap<>();

    /**
     * Number of principals — DO NOT USE! Derive its value from somewhere else!
     */
    int nps;

    /**
     * mem: An array of some size to use as memory — DO NOT USE! This isn't the 90s!
     */
    String mem;

    /**
     * Replies indexed by principal id
     */
    SortedMap<String, ReplyMessage> reps = new TreeMap<>();

    /**
     * Creates a new RepInfo object
     */
    public RepInfo(PbftReplica replica) {
        this.replica = replica;
        Rinfo ri = new Rinfo(true, Instant.from(Instant.MIN));

        // Initialize the reps list
        for (String pid : replica.getNodeIds()) {
            ReplyMessage rr = new ReplyMessage(0, 0, pid);
            reps.put(pid, rr);
        }
    }

    /**
     * Creates a new RepInfo object with the given previous state
     *
     * @param prevState the previous state
     */
    public RepInfo(Object prevState) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public int size() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Returns the timestamp in the last message sent to principal "pid".
     *
     * @param pid a valid principal identifier
     * @return the timestamp
     */
    public long req_id(String pid) {
        System.out.println(reps);
        System.out.println("Returning request id for " + pid);
        // FIXME: inserting a default value if the key is not found
        return reps.computeIfAbsent(pid, k -> new ReplyMessage(0, 0, pid)).request_id();
    }

    /**
     * Returns the digest of the last reply value sent to pid.
     *
     * @param pid a valid principal identifier
     * @return the digest of the last reply value sent to pid
     */
    public Digest digest(String pid) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Returns a pointer to the last reply value sent to "pid", or null
     * if there is no such reply.
     *
     * @param pid a valid principal identifier
     * @return a pointer to the last reply value sent to "pid", or null
     */
    public ReplyMessage reply(String pid) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Updates this to reflect the new state and removes stale requests from rset.
     * If it removes the first request in "rset", returns true; otherwise, returns false.
     *
     * @param rset the set of reply messages
     * @return true if it removes the first request in "rset"; otherwise, returns false
     */
    public boolean new_state(Queue<ReplyMessage> rset) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Returns a pointer to a buffer where the new reply value for principal "pid"
     * can be placed. The lentgh of the buffer in bytes is returned in "size".
     * Sets the reply to tentative.
     *
     * @param pid a valid principal identifier
     * @return a pointer to a buffer where the new reply value for principal "pid" can be placed
     */
    public String new_reply(String pid) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Completes the construction of a new reply value: this is informed that the reply
     * value is size bytes long and computes its digest.
     *
     * @param pid a valid principal identifier
     * @param rid the request id
     */
    public void end_reply(String pid, long rid) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Mark "pid"'s last reply as committed.
     *
     * @param pid a valid principal identifier
     */
    public void commit_reply(String pid) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Returns true iff the last reply sent to "pid" is committed.
     *
     * @param pid a valid principal identifier
     * @return true if the last reply sent to "pid" is committed; otherwise, returns false.
     */
    public boolean is_committed(String pid) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Sends a reply message to "pid" for view "v" from replica "id" with the latest reply
     * stored in the buffer returned by new_reply. If tentative is omitted or true, it sends
     * the reply as tentative unless it was previously committed.
     * Requires "end_reply" was called after the last call to new_reply for "pid".
     *
     * @param pid       a valid principal identifier
     * @param v         the view number
     * @param id        the replica identifier
     * @param tentative true if last reply is tentative and was not committed
     */
    public void send_reply(String pid, long v, String id, boolean tentative) {
        ReplyMessage r = reps.computeIfAbsent(pid, k -> new ReplyMessage(0, 0, pid));
        ReplyMessage rr = new ReplyMessage(r);

        if (rr.isReadOnly || rr.isSigned || rr.getV() != 0 /*|| rr.getReplica() != null*/) {
            throw new IllegalStateException("Invalid state");
        }

        if (!tentative && ireps.get(pid).tentative) {
            ireps.get(pid).tentative = false;
            ireps.get(pid).lsent = Instant.MIN;
        }

        Instant cur;
        Instant lsent = ireps.computeIfAbsent(pid, k -> new Rinfo(false, Instant.MIN)).lsent;

        if (!lsent.equals(Instant.MIN)) {
            cur = replica.getCurrentTime();
            if (cur.isAfter(lsent.plusMillis(10))) { // FIXME: magic constant
                return;
            }
            ireps.get(pid).lsent = cur;
        }

        if (ireps.get(pid).tentative) r.setReadOnly(true);
        rr.setV(v);
        rr.setReplica(id);
        Principal p = replica.getPrincipal(pid); // TODO: use this to sign message

        replica.sendMessage(r, pid);

        // Undo changes - not needed; to be garbage collected!
        rr.setReadOnly(false);
        rr.setSigned(false);
        rr.setV(0);
        rr.setReplica(null);
    }

    /**
     * Sends a reply message to "pid" for view "v" from replica "id" with the latest reply
     * stored in the buffer returned by new_reply.
     * The reply is sent as tentative unless it was previously committed.
     * Requires "end_reply" was called after the last call to new_reply for "pid".
     *
     * @param pid a valid principal identifier
     * @param v   the view number
     * @param id  the replica identifier
     */
    public void send_reply(String pid, long v, String id) {
        send_reply(pid, v, id, true);
    }

    /**
     * Get a pointer to the beggining of the mem region used to store the replies.
     *
     * @return a pointer to the beggining of the mem region used to store the replies
     */
    public String rep_info_mem() {
        // XXX: #ifndef NO_STATE_TRANSLATION
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Reply information
     */
    @Data
    @AllArgsConstructor
    public class Rinfo {
        private boolean tentative;
        private Instant lsent;
    }

}
