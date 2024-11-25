package byzzbench.simulator.protocols.tendermint.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class PrevoteMessage extends GenericVoteMessage {
    private final long height;      // Current blockchain height
    private final long round;       // Current round in the consensus process
    private final String replicaId; // ID of the validator sending the prevote

    @Override
    public String getType() {
        return "PREVOTE";
    }

    @Override
    public String getAuthor() {
        return replicaId;
    }
}
