package byzzbench.simulator.protocols.pbft.message;

import byzzbench.simulator.protocols.pbft.CertifiableMessage;
import byzzbench.simulator.protocols.pbft.PbftMessagePayloadWithSequenceNumber;
import byzzbench.simulator.protocols.pbft.PbftReplica;
import byzzbench.simulator.transport.MessagePayload;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

/**
 * A Commit message from the Replicas to the Replicas: see Commit.h/cc.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@With
@AllArgsConstructor
public class CommitMessage extends MessagePayload implements CertifiableMessage, PbftMessagePayloadWithSequenceNumber {
    public static final String TYPE = "Commit";
    /**
     * The view number
     */
    private final long view;

    /**
     * The sequence number
     */
    private final long seqno;

    /**
     * The ID of the replica that generated this message
     */
    private final String id;

    /**
     * A variable length signature
     */
    private final String signature;

    /**
     * Creates a new Commit message with view number "v" and sequence number "s".
     *
     * @param replica the replica that is creating this message
     * @param v       the view number
     * @param s       the sequence number
     */
    public CommitMessage(PbftReplica replica, long v, long s) {
        this.view = v;
        this.seqno = s;
        this.id = replica.id();
        this.signature = replica.id(); // TODO: sign properly
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean match(CertifiableMessage other) {
        return false;
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
}
