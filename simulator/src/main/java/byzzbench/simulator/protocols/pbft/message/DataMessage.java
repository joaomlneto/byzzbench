package byzzbench.simulator.protocols.pbft.message;

import byzzbench.simulator.transport.MessagePayload;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * A Data message: see Data.h/cc.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public abstract class DataMessage extends MessagePayload {
    public static final String TYPE = "Data";
    /**
     * Index of this page within level
     */
    private final int index;

    /**
     * Seqno of last checkpoint in which data was modified
     */
    private final long lastModifiedSeqno;

    /**
     * Data
     */
    private final byte[] data;


    @Override
    public String getType() {
        return TYPE;
    }
}
