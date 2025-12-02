package byzzbench.simulator.protocols.fab2.messages;

import byzzbench.simulator.exploration_strategy.byzzfuzz.MessageWithByzzFuzzRoundInfo;
import byzzbench.simulator.protocols.fab2.Pair;
import byzzbench.simulator.protocols.pbft_java.message.IPhaseMessage;
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
public class LearnMessage extends IPhaseMessage implements MessageWithByzzFuzzRoundInfo {
    private final Pair valueAndProposalNumber;

    public String getType() {
        return "LEARN";
    }

    @Override
    public long getViewNumber() {
        return valueAndProposalNumber.getProposalNumber().getViewNumber();
    }

    @Override
    public long getSequenceNumber() {
        return valueAndProposalNumber.getProposalNumber().getSequenceNumber();
    }

    @Override
    public byte[] getDigest() {
        return valueAndProposalNumber.getValue();
    }

    @Override
    public long getRound() {
        return 5 * (getSequenceNumber() - 1) + 3;
    }
}
