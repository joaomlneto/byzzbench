package byzzbench.simulator.protocols.pbft.message;

import byzzbench.simulator.protocols.pbft.CertifiableMessage;
import byzzbench.simulator.protocols.pbft.Digest;
import byzzbench.simulator.protocols.pbft.IdentifiableObject;
import byzzbench.simulator.protocols.pbft.PbftReplica;
import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

/**
 * A Checkpoint message from the Replicas to the Replicas: see Checkpoint.h/cc.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@With
public class CheckpointMessage extends MessagePayload implements CertifiableMessage, IdentifiableObject {
    public static final String TYPE = "Checkpoint";
    /**
     * The sequence number
     */
    private final long seqno;

    /**
     * The digest
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
     * Whether the sender of the message believes the checkpoint is stable
     */
    private final boolean isStable;

    /**
     * Creates a new signed Checkpoint message with sequence number "s"
     * and digest "d". "stable" should be true iff the checkpoint is known
     * to be stable.
     *
     * @param s      the sequence number
     * @param d      the digest
     * @param stable whether the checkpoint is stable
     */
    public CheckpointMessage(PbftReplica replica, long s, Digest d, boolean stable) {
        this.isStable = stable;
        this.seqno = s;
        this.digest = d;
        this.id = replica.id();

        // TODO: signatures
        this.signature = replica.id();
    }

    public boolean stable() {
        return this.isStable;
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
        return this.id;
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

    public long seqno() {
        return this.seqno;
    }
}
