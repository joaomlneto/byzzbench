package byzzbench.simulator.protocols.pbft;

import byzzbench.simulator.protocols.pbft.message.NewViewMessage;
import byzzbench.simulator.protocols.pbft.message.ViewChangeMessage;
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
    private long v;
    /**
     * The new view message
     */
    private NewViewMessage nv;
    /**
     * Number of view-change messages in nv
     */
    private int vc_target;
    /**
     * Number of view-change messages already matched with target
     */
    private int vc_cur;
    /**
     * Buffer for view changes associated with "nv" and their acks
     */
    private List<VCInfo> vcs;

    /**
     * Array of candidate checkpoints
     */
    private List<CkptSum> ckpts;

    /**
     * Index of chosen checkpoint (-1 if no checkpoint chosen.)
     */
    private int chosen_ckpt;

    /**
     * Sequence number of chosen checkpoint
     */
    private long min;

    /**
     * All requests that will propagate to the next view have
     * sequence number less than max
     */
    private long max;

    /**
     * reqs and comp_reqs are indexed by sequence number minus base
     */
    private long base;

    /**
     * An array for each sequence number above min and less than or equal to max
     */
    private List<List<ReqSum>> reqs;

    /**
     * For each row index in reqs, contains either -1 if no request with complete
     * information for that index or the column index of the complete Req_sum
     */
    private List<Integer> comp_reqs;

    /**
     * Number of complete entries with seqnos between min and max
     */
    private int n_complete;

    /**
     * Pointer to parent view-info
     */
    private ViewInfo vi;

    /**
     * True iff contains all the necessary information for a new-view
     */
    private boolean is_complete;

    /**
     * Time at which my new-view was last sent
     */
    private Instant nv_sent;

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
