package byzzbench.simulator.protocols.pbft.message;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * A Data message: see Data.h/cc.
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class BASEDataMessage extends DataMessage {
    public BASEDataMessage(int index, long lastModifiedSeqno, byte[] data) {
        super(index, lastModifiedSeqno, data);
    }
}
