package byzzbench.simulator.protocols.hbft.message;

import byzzbench.simulator.protocols.hbft.SpeculativeHistory;

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
    private final RequestMessage request;
    private final String replicaId;
    // Speculative execution history
    private final SpeculativeHistory speculativeHistory;
    /*  
     * As of hBFT 4.4 PREPARE is included in the COMMIT
     * Almost-MAC agreement
     * 
     * Should be signed by primary so cannot be faked
     */
    private final PrepareMessage prepare;

    @Override
    public String getType() {
        return "COMMIT";
    }
}
