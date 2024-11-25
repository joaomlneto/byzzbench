package byzzbench.simulator.protocols.tendermint.message;

import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class ProposalMessage extends MessagePayload {
    private final String replicaId; // ID of the proposer
    private final long height; // Current blockchain height
    private final long round; // Current round in the consensus process
    private final int value; // Value to be proposed
    private final long validRound; // Round in which the value is valid

    @Override
    public String getType() {
        return "PROPOSAL";
    }
}
