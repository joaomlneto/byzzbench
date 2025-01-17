package byzzbench.simulator.protocols.fab.messages;

import byzzbench.simulator.protocols.fab.Pair;
import byzzbench.simulator.protocols.pbft_java.message.IPhaseMessage;
import byzzbench.simulator.transport.MessagePayload;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

/**
 * <p>Message sent by Learner replicas to Proposer and (the other) Learner replicas to inform them that a value has been learned.</p>
 */
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@With
public class LearnMessage extends IPhaseMessage {
    private final Pair valueAndProposalNumber;
    public String getType() {
        return "LEARN";
    }

    @Override
    public long getViewNumber() {
        return valueAndProposalNumber.getNumber();
    }

    @Override
    public long getSequenceNumber() {
        return valueAndProposalNumber.getNumber();
    }

    @Override
    public byte[] getDigest() {
        return valueAndProposalNumber.getValue();
    }
}
