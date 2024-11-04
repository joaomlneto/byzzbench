package byzzbench.simulator.protocols.hbft.message;

import java.util.Collection;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class CommitMessage extends IPhaseMessage {
    private final long viewNumber;
    private final long sequenceNumber;
    private final byte[] digest;
    // Might not be needed
    private final String message;
    private final String replicaId;
    private final String clientId;
    // Speculative execution history
    private final Collection<RequestMessage> speculativeHistory;

    @Override
    public String getType() {
        return "COMMIT";
    }
}
