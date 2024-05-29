package byzzbench.runner.protocols.fasthotstuff.message;

import byzzbench.runner.transport.MessagePayload;
import lombok.Data;
import lombok.With;

@Data
@With
public class VoteMessage implements MessagePayload, GenericVoteMessage {
    private final String author;
    private final String blockHash;

    @Override
    public String getType() {
        return "VOTE";
    }
}
