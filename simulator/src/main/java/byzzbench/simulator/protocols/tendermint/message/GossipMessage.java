package byzzbench.simulator.protocols.tendermint.message;

import byzzbench.simulator.exploration_strategy.byzzfuzz.MessageWithByzzFuzzRoundInfo;
import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class GossipMessage extends MessagePayload implements MessageWithByzzFuzzRoundInfo {
    private final String replicaId;
    private final GenericMessage gossipMessage;


    @Override
    public String getType() {
        return "GOSSIP " + gossipMessage.getType();
    }

    @Override
    public long getViewNumber() {
        return 0;
    }

    @Override
    public long getRound() {
        return 0;
    }
}
