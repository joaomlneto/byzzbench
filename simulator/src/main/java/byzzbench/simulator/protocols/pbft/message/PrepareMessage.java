package byzzbench.simulator.protocols.pbft.message;

import byzzbench.simulator.protocols.pbft.*;
import byzzbench.simulator.transport.MessagePayload;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

/**
 * A Prepare message from the Primary to the Replicas: see Prepare.h/cc.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@With
@AllArgsConstructor
public class PrepareMessage extends MessagePayload implements CertifiableMessage, PbftMessagePayloadWithSequenceNumber {
    public static final String TYPE = "Prepare";
    /**
     * The view number
     */
    private final long view;

    /**
     * Whether the request is read-only.
     * This is computed as "extra & 1" in the original code.
     */
    private final boolean isReadOnly;

    /**
     * Whether the request is signed.
     * This is computed as "extra & 2" in the original code.
     */
    private final boolean isSigned;

    /**
     * The sequence number
     */
    private final long seqno;

    /**
     * Digest
     */
    private final Digest digest;

    /**
     * The ID of the replica that generated this message
     */
    private final String id;

    /**
     * A variable length signature
     */
    private final String signature;

    /**
     * Creates a new signed Prepare message with given view number, sequence number and digest.
     * "dst" should be non-null iff prepare is sent to a single replica "dst" as proof
     * of authenticity for a request.
     *
     * @param replica the replica that is creating this message
     * @param v       the view number
     * @param s       the sequence number
     * @param d       the digest
     * @param dst     the destination replica (may be null)
     */
    public PrepareMessage(PbftReplica replica, long v, long s, Digest d, Principal dst) {
        this.isReadOnly = dst != null;
        this.view = v;
        this.seqno = s;
        this.digest = d;

        this.id = replica.id();

        if (dst == null) {
            this.signature = this.id;
        } else {
            this.signature = this.id;
        }

        throw new UnsupportedOperationException("Not implemented");
    }


    /**
     * Creates a new signed Prepare message with given view number, sequence number and digest.
     * Prepare should not be sent to a single replica as proof of authenticity for a request.
     *
     * @param replica the replica that is creating this message
     * @param v       the view number
     * @param s       the sequence number
     * @param d       the digest
     */
    public PrepareMessage(PbftReplica replica, long v, long s, Digest d) {
        this(replica, v, s, d, null);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean match(CertifiableMessage other) {
        if (!(other instanceof PrepareMessage p)) {
            return false;
        }

        if (view != p.view || seqno != p.seqno) {
            throw new AssertionError("Invalid argument");
        }

        return this.getView() == p.getView()
                && this.getSeqno() == p.getSeqno()
                && this.getDigest().equals(p.digest);
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public long seqno() {
        return this.seqno;
    }

    @Override
    public boolean verify() {
        return false;
    }

    @Override
    public long view() {
        return this.view;
    }

    @Override
    public boolean full() {
        return false;
    }

    @Override
    public boolean encode() {
        return false;
    }

    @Override
    public boolean decode() {
        return false;
    }

    /**
     * Fetches the digest from the message
     *
     * @return
     */
    public Digest digest() {
        return this.digest;
    }

    /**
     * Checks if this was sent as a proof of authenticity for a request
     *
     * @return true if this was sent as a proof of authenticity for a request, false otherwise
     */
    public boolean is_proof() {
        return this.isReadOnly || this.isSigned;
    }
}
