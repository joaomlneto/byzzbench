package byzzbench.simulator.protocols.fab.messages;

import byzzbench.simulator.protocols.fab.replicas.Pair;
import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class AcceptMessage extends MessagePayload {
    private final String replicaId;
    private final Pair valueAndProposalNumber;

    public String getType() {
        return "ACCEPT";
    }
}
