package byzzbench.simulator.protocols.tendermint.message;

import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class GossipMessage extends MessagePayload {
    private final String replicaId;
    private final GenericMessage gossipMessage;

    @Override
    public String getType() {
        return "Gossip";
    }
}
