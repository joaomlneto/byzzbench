package byzzbench.simulator.protocols.fab.messages;

import byzzbench.simulator.protocols.fab.replica.Pair;
import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

/**
 * <p>Message sent by Proposer replicas to (the other) Proposer replicas to inform them that a value has been accepted.</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@With
public class SatisfiedMessage extends MessagePayload {
    private final String senderId;
    private final Pair valueAndProposalNumber;
    public String getType() {
        return "SATISFIED";
    }
}
