package byzzbench.simulator.protocols.pbft_java.message;

import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.With;

@Data
@With
public class PhaseMessage extends IPhaseMessage {
    private final long viewNumber;
    private final long sequenceNumber;
    private final byte[] digest;

    @Override
    public String getType() {
        return "PHASE";
    }
}
