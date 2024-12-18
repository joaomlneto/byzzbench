package byzzbench.simulator.protocols.tendermint.message;

import byzzbench.simulator.transport.DefaultClientRequestPayload;
import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class GossipRequest extends MessagePayload {
    private final String replicaId;
    private final RequestMessage request;

    @Override
    public String getType() {
        return "GOSSIP REQUEST";
    }
}
