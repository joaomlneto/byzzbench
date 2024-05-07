package bftbench.runner.protocols.pbft.message;

import bftbench.runner.transport.MessagePayload;
import lombok.Data;
import lombok.With;

@Data
@With
public class PhaseMessage implements MessagePayload, IPhaseMessage {
    private final long viewNumber;
    private final long sequenceNumber;
    private final byte[] digest;

    @Override
    public String getType() {
        return "PHASE";
    }
}
