package byzzbench.simulator.protocols.tendermint.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class PrevoteMessage extends GenericMessage {
    private final long height;      // Current blockchain height
    private final long sequence;       // Current round in the consensus process
    private final long totalSeq;
    private final String replicaId; // ID of the validator sending the prevote
    private final Block block;      // Block being prevoted

    @Override
    public String getType() {
        return "PREVOTE";
    }

    @Override
    public String getAuthor() {
        return replicaId;
    }

    @Override
    public long getRound() {
        return totalSeq + sequence + 2;
    }
}
