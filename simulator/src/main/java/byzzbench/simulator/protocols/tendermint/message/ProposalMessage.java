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
    private final String blockHash; // Hash of the proposed block

    @Override
    public String getType() {
        return "PROPOSAL";
    }
}
