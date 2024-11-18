package byzzbench.simulator.protocols.tendermint.message;

import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class CommitMessage extends MessagePayload {
    private final String replicaId;  // The ID of the validator sending the commit message
    private final long height;       // The height of the blockchain
    private final String blockHash;  // The hash of the committed block

    @Override
    public String getType() {
        return "COMMIT";
    }
}
