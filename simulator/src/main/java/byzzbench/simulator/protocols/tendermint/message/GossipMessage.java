package byzzbench.simulator.protocols.tendermint.message;

import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.transport.messages.MessageWithRound;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class GossipMessage extends MessagePayload implements MessageWithRound {
    private final String replicaId;
    private final GenericMessage gossipMessage;


    @Override
    public String getType() {
        return "GOSSIP " + gossipMessage.getType();
    }

    @Override
    public long getRound() {
        return gossipMessage.getRound();
    }
}
