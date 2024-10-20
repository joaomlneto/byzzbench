package byzzbench.simulator.protocols.pbft.message;

import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

/**
 * A QueryStable message: see Query_stable.h/cc.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@With
public class QueryStableMessage extends MessagePayload {
    public static final String TYPE = "QueryStable";
    /**
     * Identifier of the replica that generated the message.
     */
    private final int replicaId;

    /**
     * A nonce
     */
    private final int nonce;

    /**
     * Signature
     */
    private final byte[] signature;

    @Override
    public String getType() {
        return TYPE;
    }
}
