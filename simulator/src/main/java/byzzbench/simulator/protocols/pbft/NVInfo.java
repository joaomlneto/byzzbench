package byzzbench.simulator.protocols.pbft;

import byzzbench.simulator.protocols.pbft.message.*;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Holds information concerning a specific new-view message
 */
public class NVInfo {
    /**
     * The view number
     */
    private final long v;
    /**
     * The new view message
     */
    private final NewViewMessage nv;
    /**
     * Number of view-change messages in nv
     */
    private final int vc_target;
    /**
     * Number of view-change messages already matched with target
     */
    private final int vc_cur;
    /**
     * Buffer for view changes associated with "nv" and their acks
     */
    private final List<VCInfo> vcs;

    /**
     * Array of candidate checkpoints
     */
    private final List<CkptSum> ckpts;

    /**
     * Index of chosen checkpoint (-1 if no checkpoint chosen.)
     */
    private final int chosen_ckpt;

    /**
     * Sequence number of chosen checkpoint
     */
    private final long min;

    /**
     * All requests that will propagate to the next view have
     * sequence number less than max
     */
    private final long max;

    /**
     * reqs and comp_reqs are indexed by sequence number minus base
     */
    private final long base;

    /**
     * An array for each sequence number above min and less than or equal to max
     */
    private final List<List<ReqSum>> reqs;

    /**
     * For each row index in reqs, contains either -1 if no request with complete
     * information for that index or the column index of the complete Req_sum
     */
    private final List<Integer> comp_reqs;

    /**
     * Number of complete entries with seqnos between min and max
     */
    private final int n_complete;

    /**
     * Pointer to parent view-info
     */
    private final ViewInfo vi;

    /**
     * True iff contains all the necessary information for a new-view
     */
    private final boolean is_complete;

    /**
     * Time at which my new-view was last sent
     */
    private final Instant nv_sent;

    /**
     * Creates an empty NVInfo object
     */
    public NVInfo() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Makes this empty — deletes all contained messages and sets view() == 0
     */
    public void clear() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * If "is_complete()", does nothing. Otherwise, deletes all messages contained in this
     * and sets view() == 0. Except that it does not delete and it returns any view-change
     * message from replica "id".
     *
     * @param id the identifier of the replica
     * @return the view-change message from replica "id"
     */
    public ViewChangeMessage mark_stale(String id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * If there is a new-view message stored in "this", returns it. Otherwise, returns null
     *
     * @return the new-view message stored in "this" or null
     */
    public NewViewMessage new_view() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * If there is any view-change message from replica "id" stored in this, returns it.
     * Otherwise, returns null.
     *
     * @param id the identifier of the replica
     * @return the view-change message from replica "id" or null
     */
    public ViewChangeMessage view_change(String id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Returns the view number of the messages currently stored on this, or 0 if
     * this is empty (i.e., if it does not contain any new-view message).
     *
     * @return the view number of the messages currently stored on this
     */
    public long view() {
        return v;
    }

    /**
     * Returns true iff this contains all the necessary information to move to the new-view.
     *
     * @return true if this contains all the necessary information to move to the new-view. False otherwise.
     */
    public boolean complete() {
        return is_complete;
    }

    /**
     * Mark this as complete in view "v".
     *
     * @param v the view number
     */
    public void make_complete(long v) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * If "nv-view() <= view(), it does not modify this and deletes "nv".
     * Otherwise, it adds "nv" to this and if "view() != 0", it deletes any new-view
     * and view-change messages stored in this.
     * Requires "nv.verify() || node->id() == node->primary(nv->view())"
     *
     * @param nv     the new-view message
     * @param parent the parent view-info
     * @return true iff it adds "nv" to this
     */
    public boolean add(NewViewMessage nv, ViewInfo parent) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * If "vc" is one of the messages reference in the new-view message contained
     * in this and is valid, add "vc" to this and return true.
     * Otherwise, do nothing and return false.
     * Requires "vc->view() == view()" and "verified == vc->verify()"
     *
     * @param vc       the view-change message
     * @param verified true if the view-change message is verified; otherwise, false
     * @return true if the view-change message is added; otherwise, false
     */
    public boolean add(ViewChangeMessage vc, boolean verified) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * If there is no view-change corresponding to "vca" in this or referenced by a
     * new-view in this, returns false. Otherwise, it inserts "vca" in this (if its
     * digest matches the view-change's) or deletes vca (otherwise) and returns true.
     *
     * @param vca the view-change acknowledgement message
     * @return true if the view-change acknowledgement message is added; otherwise, false
     */
    public boolean add(ViewChangeAcknowledgementMessage vca) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Sets "d" to the digest of the request with sequence number "n". If enough information
     * to make a pre-prepare is available, it returns an appropriate pre-prepare.
     * Otherwise, returns null.
     *
     * @param n the sequence number of the request
     * @param d the digest of the request
     * @return the pre-prepare message if available; otherwise, null
     */
    public PrePrepareMessage fetch_request(long n, Digest d) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Checks if "pp" is a pre-prepare that is needed to complete a view-change. If it is,
     * stores "pp", otherwise deletes "pp".
     *
     * @param pp the pre-prepare message
     */
    public void add_missing(PrePrepareMessage pp) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Records that the big request with digest "rd" that is referenced by a
     * pre-prepare with sequence number "n" as the i-th big request is cached.
     *
     * @param rd the digest of the big request
     * @param n  the sequence number of the request
     * @param i  the index of the request in the sequence number
     */
    public void add_missing(Digest rd, long n, int i) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Checks if "p" is a prepare that is needed to complete a view-change.
     * If it is, stores "pp".
     *
     * @param p the prepare message
     */
    public void add_missing(PrepareMessage p) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Mutates "m" to record which view change messages were accepted in the
     * current view or are not needed to complete new-view.
     *
     * @param m the status message
     */
    public void set_received_vcs(StatusMessage m) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Mutates "m" to record which pre-prepares are missing
     *
     * @param m the status message
     */
    public void set_missing_pps(StatusMessage m) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Informs "this" that checkpoint sequence number "ls" is stable
     *
     * @param ls the sequence number of the checkpoint
     */
    public void mark_stable(long ls) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Summarizes the information in "vc" and combines it with
     * the information from other view-change messages
     *
     * @param vc the view-change message
     */
    private void summarize(ViewChangeMessage vc) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Summarize the request information in "vc" and combine it
     * with information from other view-change messages.
     *
     * @param vc the view-change message
     */
    private void summarize_req(ViewChangeMessage vc) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * If "c" has a higher max_stable than sum->ckpts[chosen], make it
     * the new chosen checkpoint. Otherwise, do nothing.
     * Requires that "c = sum->ckpts[index]" has n_proofs and n_le greater
     * than or equal to "node->n_f()".
     *
     * @param index the index of the checkpoint
     */
    private void choose_ckpt(int index) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Checks if the request corresponding to cur has enough information to
     * be propagated to the next view. If it does and complete() becomes true, calls
     * make_new_view.
     * Requires !complete().
     *
     * @param cur the request summary
     * @param i   the sequence number of the request
     * @param j   the index of the request in the sequence number
     */
    private void check_comp(ReqSum cur, long i, int j) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Completes the construction of the new-view message.
     * Requires complete().
     */
    private void make_new_view() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Checks if this contains a correct new-view and, if so, returns true
     *
     * @return true if this contains a correct new-view; otherwise, returns false
     */
    private boolean check_new_view() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Update proofs for cur with information in "vc" for sequence number "i".
     *
     * @param cur the request summary
     * @param vc  the view-change message
     * @param i   the sequence number of the request
     */
    private void get_proofs(ReqSum cur, ViewChangeMessage vc, long i) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Computes the maximum sequence number that is known to be stable.
     *
     * @return maximum sequence number that is known to be stable
     */
    private long known_stable() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Summary about request
     */
    @Data
    private static class ReqSum {
        private long v;
        private Digest d;
        private int n_proofs;

        /**
         * Number of positive proofs
         */
        private int n_pproofs;

        /**
         * Replicas that sent positive proofs.
         */
        private SortedSet<String> r_pproofs = new TreeSet<>();

        private PrePrepareInfo pi;
        private int n_le;

        /**
         * Identifier of the first replica proposing this
         */
        private String id;
    }

    /**
     * Information about a view change message
     */
    @Data
    private static class VCInfo implements SeqNumLog.SeqNumLogEntry {
        ViewChangeMessage vc;
        int ack_count;
        Set<String> ack_reps;
        boolean req_sum;

        public VCInfo() {
            throw new UnsupportedOperationException("Not implemented");
        }

        public void clear() {
            throw new UnsupportedOperationException("Not implemented");
        }
    }

    /**
     * Data structures to check the correctness and completeness of new-view messages.
     */
    @Data
    private static class CkptSum {
        private long n;
        private Digest d;
        private int n_proofs;
        private int n_le;
        private long max_seqno;

        /**
         * Identifier of first replica proposing this
         */
        private String id;
    }
}
