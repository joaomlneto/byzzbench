package byzzbench.simulator.protocols.pbft;

import byzzbench.simulator.protocols.pbft.message.PrePrepareMessage;
import byzzbench.simulator.protocols.pbft.message.RequestMessage;
import lombok.Data;

import java.io.Serializable;
import java.util.*;

public class BigReqTable implements Serializable {
    /**
     * The replica that owns this big request table
     */
    private final transient PbftReplica replica;

    /**
     * Maximum number of entries allowed in the table
     */
    private final int max_entries;
    /**
     * Requests that have no waiting pre-prepares, indexed by client ID.
     */
    private final SortedMap<String, RequestMessage> unmatched = new TreeMap<>();
    /**
     * Table of big requests
     */
    private SortedMap<Digest, BigReqEntry> breqs;

    /**
     * Creates a new big request table with a maximum number of entries
     *
     * @param replica the replica that owns this big request table
     */
    public BigReqTable(PbftReplica replica) {
        this.replica = replica;
        this.max_entries = replica.getConfig().getMAX_OUT() * PrePrepareMessage.big_req_max;
    }

    /**
     * Records that request "r" is referenced by the pre-prepare with sequence number "n"
     * and that this information is current in view "v".
     *
     * @param r the request
     * @param n the sequence number
     * @param v the view number
     */
    public void add_pre_prepare(RequestMessage r, long n, long v) {
        Digest rd = r.digest();
        if (breqs.containsKey(rd)) {
            BigReqEntry bre = breqs.get(rd);
            remove_unmatched(bre);

            if (n > bre.maxn) {
                bre.maxn = n;
            }

            if (v > bre.maxv) {
                bre.maxv = v;
            }

            if (bre.r == null) {
                bre.r = r;
            } // else there is nothing to do: just discard the RequestMessage r
        } else {
            // No entry in breqs for rd
            BigReqEntry bre = new BigReqEntry();
            bre.rd = rd;
            bre.r = r;
            bre.maxn = n;
            bre.maxv = v;
            breqs.put(rd, bre);
        }
    }

    /**
     * Records that the i-th reference to a big request in the
     * pre-prepare with sequence number "n" is to the request with
     * digest "rd", and that this information is current in view
     * "v".
     *
     * @param rd the digest of the request
     * @param i  the index of the reference
     * @param n  the sequence number
     * @param v  the view number
     * @return true if the request is in the table; otherwise, returns false
     */
    public boolean add_pre_prepare(Digest rd, int i, long n, long v) {
        if (breqs.containsKey(rd)) {
            BigReqEntry bre = breqs.get(rd);
            this.remove_unmatched(bre);

            if (n > bre.maxn) {
                bre.maxn = n;
            }

            if (v > bre.maxv) {
                bre.maxv = v;
            }

            if (bre.r != null) {
                return true;
            } else {
                WaitingPP wp = new WaitingPP(i, n);
                bre.waiting.add(wp);
            }
        } else {
            // No entry in breqs for rd
            BigReqEntry bre = new BigReqEntry();
            bre.rd = rd;
            WaitingPP wp = new WaitingPP(i, n);
            bre.waiting.add(wp);
            bre.maxn = n;
            bre.maxv = v;
            breqs.put(rd, bre);
        }

        return false;
    }

    /**
     * If there is an entry for digest "r->digest()", the entry
     * does not already contain a request and the authenticity of the
     * request can be verified, then it adds "r" to the entry, calls
     * "add_request" on each pre-prepare-info whose pre-prepare is
     * waiting on the entry, and returns true. Otherwise, returns false
     * and has no other effects (in particular it does not delete "r").
     * Requires r->size() > Request::big_req_thresh & verified == r->verify()
     *
     * @param r        the request
     * @param verified true if the request is verified; otherwise, false
     * @return true if the request was added; otherwise, returns false
     */
    public boolean add_request(RequestMessage r, boolean verified) {
        throw new UnsupportedOperationException("Not implemented");
    }


    /**
     * If there is an entry for digest "r->digest()", the entry
     * does not already contain a request and the authenticity of the
     * request can be verified, then it adds "r" to the entry, calls
     * "add_request" on each pre-prepare-info whose pre-prepare is
     * waiting on the entry, and returns true. Otherwise, returns false
     * and has no other effects (in particular it does not delete "r").
     * Requires r->size() > Request::big_req_thresh & verified == r->verify()
     *
     * @param r the request
     * @return true if the request was added; otherwise, returns false
     */
    public boolean add_request(RequestMessage r) {
        return add_request(r, true);
    }

    /**
     * Returns the request in this with digest "rd" or null if there
     * is no such request
     *
     * @param d the digest of the request
     * @return the request in this with digest "rd" or null if there is no such request
     */
    public Optional<RequestMessage> lookup(Digest d) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Discards entries that were only referred to by pre-prepares that were discarded
     * due to checkpoint "ls" becoming stable
     *
     * @param ls the sequence number of the stable checkpoint
     */
    public void mark_stable(long ls) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Discards entries that were only referred to by
     * pre-prepares that were discarded due to view changing to view "v"
     *
     * @param v the view number
     */
    public void view_change(long v) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Returns true iff there is some pre-prepare in bre->waiting that has
     * f matching prepares in its prepared certificate.
     *
     * @param bre the big request entry
     * @return true iff there is some pre-prepare in bre->waiting that has
     */
    private boolean check_pcerts(BigReqEntry bre) {
        if (!replica.hasNewView()) {
            throw new AssertionError("Replica does not have a new view");
        }

        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Zeroes entry in unmatched. Requires bre-> != 0.
     *
     * @param bre the big request entry
     */
    private void remove_unmatched(BigReqEntry bre) {
        if (bre.maxn < 0) {
            if (bre.r == null) {
                throw new IllegalStateException("Big request entry has no request");
            }
            this.unmatched.remove(bre.r.getCid());
        }
    }

    public static class BigReqEntry {
        /**
         * Request's digest
         */
        Digest rd;

        /**
         * Request, or null if request not received
         */
        RequestMessage r = null;

        /**
         * If r == null, sequence numbers of pre-prepares waiting for request
         */
        List<WaitingPP> waiting = new ArrayList<>();

        /**
         * Maximum seqno of pre-prepare referencing request
         */
        long maxn = -1;

        /**
         * Maximum view in which this entry was marked useful
         */
        long maxv = -1;
    }

    @Data
    public static class WaitingPP {
        private final long n;
        private final long i;
    }
}
