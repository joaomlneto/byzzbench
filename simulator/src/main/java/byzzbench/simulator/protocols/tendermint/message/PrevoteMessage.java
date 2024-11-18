package byzzbench.simulator.protocols.tendermint.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class PrevoteMessage extends GenericVoteMessage {
    private final String replicaId; // ID of the validator sending the prevote
    private final long height;      // Current blockchain height
    private final long round;       // Current round in the consensus process
    private final String blockHash; // Hash of the block being voted on (or null for NIL vote)

    @Override
    public String getType() {
        return "PREVOTE";
    }

    @Override
    public String getBlockHash() {
        return blockHash;
    }

    @Override
    public String getAuthor() {
        return replicaId;
    }
}
