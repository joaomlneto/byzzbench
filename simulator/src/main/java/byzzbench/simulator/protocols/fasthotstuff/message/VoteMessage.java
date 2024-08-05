package byzzbench.simulator.protocols.fasthotstuff.message;

import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.With;

@Data
@With
public class VoteMessage extends GenericVoteMessage {
    private final String author;
    private final String blockHash;

    @Override
    public String getType() {
        return "VOTE";
    }
}
