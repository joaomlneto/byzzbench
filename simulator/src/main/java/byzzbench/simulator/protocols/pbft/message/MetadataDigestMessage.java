package byzzbench.simulator.protocols.pbft.message;

import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

/**
 * A MetadataDigest message: see Meta_data_d.h/cc. This message contains digests
 * of a partition for all the checkpoints in the state of the sending replica.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@With
public class MetadataDigestMessage extends MessagePayload {
    public static final String TYPE = "MetadataDigest";
    /**
     * The timestamp of the fetch request (rid)
     */
    private final long requestId;

    /**
     * Sequence number of the last checkpoint known to be stable at sender (ls)
     */
    private final long lastStableCheckpoint;

    /**
     * The level of the partition in the hierarchy (l)
     */
    private final int level;

    /**
     * The index of the partition within the level (i)
     */
    private final int index;

    /**
     * Digests for partition for each checkpoint held by the sender in order of
     * increasing sequence number. A null digest means the sender does not have
     * the corresponding checkpoint state.
     */
    private final byte[][] digests;

    /**
     * The ID of the sender (id)
     */
    private final String senderId;

    @Override
    public String getType() {
        return TYPE;
    }
}
