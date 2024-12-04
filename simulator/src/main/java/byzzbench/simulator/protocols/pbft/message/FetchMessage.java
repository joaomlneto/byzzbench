package byzzbench.simulator.protocols.pbft.message;

import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

/**
 * A Fetch message: see Fetch.h/cc.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@With
public class FetchMessage extends MessagePayload {
    public static final String TYPE = "Fetch";
    /**
     * Request sequence number to prevent replays
     */
    private final long requestId;

    /**
     * Level of partition
     */
    private final int level;

    /**
     * Index of partition within level
     */
    private final int index;

    /**
     * Information for partition is up-to-date till seqno lu
     */
    private final long lastUpToDateSeqno;

    /**
     * Specific checkpoint requested (-1) if none
     */
    private final long requestedCheckpoint;

    /**
     * Id of designated replier (valid if c >= 0)
     */
    private final int designatedReplierId;

    /**
     * Id of the replica that generated the message.
     */
    private final String replicaId;

    /**
     * Number of the fragment we are requesting.
     * Only used in the BFT variant, and not in the BASE variant.
     * TODO: Remove this field from the BASE variant.
     */
    private final int chunkNumber;

    /**
     * An authenticator
     */
    private final byte[] authenticator;

    @Override
    public String getType() {
        return TYPE;
    }
}
