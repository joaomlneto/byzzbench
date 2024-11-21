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
    /* 
     * Probably non-standard implementation.
     * As of hbft 4.2 the CHECKPOINT message do not include
     * the speculative history, only the digest.
     * 
     * However, the paper mentions multiple times that,
     * the checkpoint sub-protocol should fix inconsistencies
     * only way for a replica to adjust its history with the checkpoint's
     * history is to have the history itself in the message, so I will include it.
     */
    private final SpeculativeHistory history;

    @Override
    public String getType() {
        return "CHECKPOINT-II";
    }
}