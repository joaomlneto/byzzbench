package byzzbench.simulator.protocols.pbft.message;

import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

/**
 * A ReplyStable message: see Reply_stable.h/cc.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@With
public class ReplyStableMessage extends MessagePayload {
    public static final String TYPE = "ReplyStable";
    /**
     * Last checkpoint at sending replica
     */
    private final long lastCheckpoint;

    /**
     * Last prepared request at sending replica
     */
    private final long lastPreparedRequest;

    /**
     * Id of sending replica
     */
    private final int replicaId;

    /**
     * Nonce in query-stable
     */
    private final int nonce;

    /**
     * MAC
     */
    private final byte[] mac;

    @Override
    public String getType() {
        return TYPE;
    }
}
