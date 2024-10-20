package byzzbench.simulator.protocols.pbft.message;

import byzzbench.simulator.protocols.pbft.CertifiableMessage;
import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

/**
 * A "Reply" message to the Client: see Reply.h/cc.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@With
public class ReplyMessage extends MessagePayload implements CertifiableMessage {
    public static final String TYPE = "Reply";
    /**
     * Current view (v)
     */
    private final long v;

    /**
     * Unique request identifier (rid).
     */
    private final String rid;

    /**
     * Digest of reply
     */
    private final byte[] digest;

    /**
     * The ID of the replica sending the reply
     */
    private final String id;

    /**
     * The reply
     */
    private final String reply;

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
}
