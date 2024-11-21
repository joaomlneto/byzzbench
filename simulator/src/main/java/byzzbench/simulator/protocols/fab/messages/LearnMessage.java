package byzzbench.simulator.protocols.fab.messages;

import byzzbench.simulator.protocols.fab.replicas.Pair;
import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

/**
 * <p>Message sent by Learner replicas to Proposer and (the other) Learner replicas to inform them that a value has been learned.</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@With
public class LearnMessage extends MessagePayload {
    private final Pair valueAndProposalNumber;
    public String getType() {
        return "LEARN";
    }
}
