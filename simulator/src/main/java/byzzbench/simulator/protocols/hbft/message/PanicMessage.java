package byzzbench.simulator.protocols.hbft.message;

import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class PanicMessage extends MessagePayload {
    private final byte[] digest;
    private final long timestamp;
    private final String clientId;

    /* 
     * Probably non-standard behavior but as of hbft 4.1,
     * the primary runs checkpoint sub-protocol if it gets
     * a PANIC from the client or 2f + 1 PANICs from other replicas
     * so we need to include the sender!
     */

    // This will be used if getSignedBy() is not correct
    // private final String senderId;

    @Override
    public String getType() {
        return "PANIC";
    }
}
