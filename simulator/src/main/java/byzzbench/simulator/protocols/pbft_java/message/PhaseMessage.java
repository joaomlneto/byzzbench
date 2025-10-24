package byzzbench.simulator.protocols.pbft_java.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public abstract class PhaseMessage extends IPhaseMessage {
    private final long viewNumber;
    private final long sequenceNumber;
    private final byte[] digest;

    @Override
    public String getType() {
        return "PHASE";
    }
}
