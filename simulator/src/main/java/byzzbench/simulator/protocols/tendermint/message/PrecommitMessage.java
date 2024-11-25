package byzzbench.simulator.protocols.tendermint.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class PrecommitMessage extends GenericVoteMessage {
    private final String replicaId; // ID of the validator sending the precommit
    private final long height;      // Current blockchain height
    private final long round;       // Current round in the consensus process
    private byte[] digest;

    @Override
    public String getType() {
        return "PRECOMMIT";
    }

    @Override
    public String getAuthor() {
        return replicaId;
    }
}
