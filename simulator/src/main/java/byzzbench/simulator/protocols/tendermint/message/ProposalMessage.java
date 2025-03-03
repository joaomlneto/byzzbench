package byzzbench.simulator.protocols.tendermint.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class ProposalMessage extends GenericMessage {
    private final String replicaId; // ID of the proposer
    private final long height; // Current blockchain height
    private final long sequence; // Current round in the consensus process
    private final long totalSeq;
    private final long validSequence; // Round in which the value is valid
    private final Block block; // Block being proposed\

    @Override
    public String getType() {
        return "PROPOSAL";
    }

    @Override
    public String getAuthor() {
        return replicaId;
    }

    @Override
    public long getRound() {
        return totalSeq + sequence + 1;
    }
}
