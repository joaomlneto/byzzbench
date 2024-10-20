package byzzbench.simulator.protocols.pbft.message;

import byzzbench.simulator.protocols.pbft.CertifiableMessage;
import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

import java.util.BitSet;

/**
 * A Status message: see Status.h/cc.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@With
public class StatusMessage extends MessagePayload implements CertifiableMessage {
    public static final String TYPE = "Status";

    public StatusMessage() {
        // TODO NOT YET IMPLEMENTED
        throw new UnsupportedOperationException("StatusMessage not yet implemented");
    }

    /**
     * Marks request with sequence number "n" is prepared (in view()).
     * It has no effect if n <= last_executed() || n > last_stable() + max_out.
     * Requires has_nv_info() == true.
     *
     * @param n sequence number
     */
    public void mark_prepared(long n) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Marks request with sequence number "n" is committed (in any view).
     * It has no effect if n <= last_executed() || n > last_stable() + max_out.
     * Requires has_nv_info() == true.
     *
     * @param n sequence number
     */
    public void mark_committed(long n) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Records that the requests whose indices are reset in brs are missing from the
     * pre-prepare with seqno n.
     *
     * @param n   seqno of pre-prepare missing big reqs
     * @param brs bitmap with missing big reqs
     */
    public void add_breqs(long n, long brs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Marks the view-change message from replica "i" for "view()"
     * (and any associated view-change acks) as having been received.
     * Requires has_vc_info() == false.
     *
     * @param i replica id
     */
    public void mark_vcs(String i) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Record that the sender is missing a pre-prepare with sequence number "n" for some
     * view greater than or equal to "v", mreqs indicates what big requests might be missing,
     * and proof whether it needs prepare messages to attest to the authenticity
     * of the corresponding request.
     * Requires: has_nv_info() == false.
     *
     * @param v     view number
     * @param n     sequence number
     * @param mreqs bitmap with missing big reqs
     * @param proof whether it needs prepare messages to attest to the authenticity
     */
    public void append_pps(long v, long n, long mreqs, boolean proof) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Authenticates message
     */
    public void authenticate() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Check if principal id() has a valid new-view message for view().
     *
     * @return true if principal id() has a valid new-view message for view(); otherwise, returns false.
     */
    public boolean has_nv_m() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Check if principal id() has the complete new-view information for view().
     *
     * @return true if principal id() has the complete new-view information for view(); otherwise, returns false.
     */
    public boolean has_nv_info() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Returns the seqno of last stable checkpoint principal id() has.
     *
     * @return the seqno of last stable checkpoint principal id() has
     */
    public long last_stable() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Returns the seqno of last request executed by principal id().
     *
     * @return the seqno of last request executed by principal id()
     */
    public long last_executed() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Check if the request with sequence number "n" was prepared (in view()) by principal id().
     * Requires has_nv_info() == true.
     *
     * @param n sequence number
     * @return true if the request with sequence number "n" was prepared (in view()) by principal id(); otherwise, returns false.
     */
    public boolean is_prepared(long n) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Check if the request with sequence number "n" was committed (in any view) by principal id().
     * Requires has_nv_info() == true.
     *
     * @param n sequence number
     * @return true if the request with sequence number "n" was committed (in any view) by principal id(); otherwise, returns false.
     */
    public boolean is_committed(long n) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Check if principal id() has a view-change message from replica "id" for "view()".
     * Requires has_vc_info() == false.
     *
     * @param i replica id
     * @return true if principal id() has a view-change message from replica "id" for "view()"; otherwise, returns false.
     */
    public boolean has_vc(String i) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean match(CertifiableMessage other) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String id() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public long view() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean verify() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean full() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean encode() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean decode() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Get a pointer to the prepared bitmap
     *
     * @return a pointer to the prepared bitmap
     */
    private BitSet prepared() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Get a pointer to the committed bitmap
     *
     * @return a pointer to the committed bitmap
     */
    private BitSet committed() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Return a pointer to the breqs array
     *
     * @return a pointer to the breqs array
     */
    private BRInfo breqs() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Return a pointer to the vcs array
     *
     * @return a pointer to the vcs array
     */
    private BitSet vcs() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Return a pointer to the pps array
     *
     * @return a pointer to the pps array
     */
    private PPInfo pps() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Set bit in set
     *
     * @param n sequence number
     * @param i index
     */
    private void mark(long n, int i) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Test bit in set
     *
     * @param n sequence number
     * @param i index
     * @return true if bit is set; otherwise, returns false.
     */
    private boolean test(long n, int i) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Information about big requests
     */
    @Data
    public class BRInfo {
        /**
         * Seqno of pre-prepare missing big reqs
         */
        private final long n;
        /**
         * Bitmap with missing big reqs
         */
        private final BitSet breqs;
    }

    /**
     * Information about pre-prepares
     */
    @Data
    public class PPInfo {
        /**
         * Minimum view of missing pre-prepare
         */
        private final long v;
        /**
         * Offset of sequence number of missing pre-prepare from ls
         */
        private final int n;
        /**
         * Non-zero if a proof of authenticity for request is needed
         */
        private final int proof;
        /**
         * Bitmap with missing big reqs
         */
        private final BitSet breqs;
    }
}
