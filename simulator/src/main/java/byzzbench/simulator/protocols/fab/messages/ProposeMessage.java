package byzzbench.simulator.protocols.fab.messages;

import byzzbench.simulator.protocols.fab.replicas.Pair;
import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

/**
 * <p>Message sent by Proposer replicas to Acceptor replicas to propose a value.</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@With
public class ProposeMessage extends MessagePayload {
    private final String replicaId;
    private final Pair valueAndProposalNumber;

    @Override
    public String getType() {
        return "PROPOSE";
    }
}
