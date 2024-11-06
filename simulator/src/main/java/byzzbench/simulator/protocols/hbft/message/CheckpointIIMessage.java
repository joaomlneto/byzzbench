package byzzbench.simulator.protocols.hbft.message;

import byzzbench.simulator.protocols.hbft.SpeculativeHistory;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class CheckpointIIMessage extends CheckpointMessage {
    private final long lastSeqNumber;
    // Digest of speculative execution history
    private final byte[] digest;
    private final String replicaId;

    @Override
    public String getType() {
        return "CHECKPOINT-II";
    }
}