package byzzbench.simulator.protocols.pbft_java.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class PrePrepareMessage extends IPhaseMessage {
    private final long viewNumber;
    private final long sequenceNumber;
    private final byte[] digest;
    private final RequestMessage request;

    @Override
    public String getType() {
        return "PRE-PREPARE";
    }
}
