package byzzbench.simulator.protocols.fasthotstuff.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class VoteMessage extends GenericVoteMessage {
    private final String author;
    private final String blockHash;

    @Override
    public String getType() {
        return "VOTE";
    }
}
