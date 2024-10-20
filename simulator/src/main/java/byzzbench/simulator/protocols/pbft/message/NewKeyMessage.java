package byzzbench.simulator.protocols.pbft.message;

import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

/**
 * A "NewKey" message see New_key.h/cc.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@With
public class NewKeyMessage extends MessagePayload {
    public static final String TYPE = "NewKey";
    /**
     * Unique request identifier (rid).
     */
    private final String requestId;

    /**
     * ID of the replica that generated the message. (id)
     */
    private final long replicaId;

    /**
     * Keys for all replicas except "id" in order of increasing identifiers.
     * Each key has size Nonce_size bytes and is encrypted with the public-key of the corresponding replica.
     */
    private final byte[] keys;

    /**
     * Signature from principal id.
     */
    private final byte[] signature;

    @Override
    public String getType() {
        return TYPE;
    }

    public boolean verify() {
        throw new UnsupportedOperationException("Not implemented");
    }
}
