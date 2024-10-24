package byzzbench.simulator.protocols.pbft;

import byzzbench.simulator.protocols.pbft.message.PrePrepareMessage;

import java.util.BitSet;
import java.util.Optional;

/**
 * Holds information about a pre-prepare and matching big requests.
 */
public class PrePrepareInfo {
    /**
     * Bitmap with bit reset for every missing request
     */
    private final BitSet mrmap = new BitSet();
    private PrePrepareMessage pp = null;
    /**
     * Number of missing requests
     */
    private int mreqs = 0;

    /**
     * Adds {@link PrePrepareMessage} "p" to this
     *
     * @param p the pre-prepare to add
     */
    public void add(PrePrepareMessage p) {
        // assert that pp == 0
        if (pp != null) {
            throw new IllegalArgumentException("Pre-prepare already exists");
        }
        pp = p;
        mreqs = p.num_big_reqs();
        mrmap.clear();

        throw new UnsupportedOperationException("Not done implemented this method!!");
    }

    /**
     * Adds "p" to this and records that all the big reqs it refers to are cached.
     *
     * @param p the pre-prepare to add
     */
    public void add_complete(PrePrepareMessage p) {
        // throw error if pp is null
        if (pp == null) {
            throw new IllegalArgumentException("Pre-prepare is null");
        }

        this.pp = p;
        mreqs = 0;
        mrmap.clear();
    }

    /**
     * If there is a pre-prepare in this and its i-th reference
     * to a big request is for the request with digest rd record
     * that this digest is cached.
     *
     * @param rd the digest of the big request
     * @param i  the index of the reference
     */
    public void add(Digest rd, int i) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Retrieve the pre-prepare message from this if it exists.
     *
     * @return the pre-prepare message if it exists, null otherwise.
     */
    public Optional<PrePrepareMessage> pre_prepare() {
        return Optional.ofNullable(pp);
    }

    /**
     * Returns a bit map with the indices of missing requests.
     *
     * @return a bit map with the indices of missing requests.
     */
    public BitSet missing_reqs() {
        return this.mrmap;
    }

    /**
     * Check if this contains a pre-prepare and all the big requests
     * it references are cached.
     *
     * @return true iff this is complete, false otherwise.
     */
    public boolean is_complete() {
        return pp != null && mreqs == 0;
    }

    /**
     * Makes this empty and deletes any pre-prepare in it.
     */
    public void clear() {
        this.pp = null;
    }

    /**
     * Makes this empty without deleting any contained pre-prepare.
     */
    public void zero() {
        this.pp = null;
    }
}
