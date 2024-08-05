package byzzbench.simulator.protocols.pbft_java.message;

import lombok.Data;
import lombok.With;

@Data
@With
public class CommitMessage extends IPhaseMessage {
    private final long viewNumber;
    private final long sequenceNumber;
    private final byte[] digest;
    private final String replicaId;

    @Override
    public String getType() {
        return "COMMIT";
    }
}
