package byzzbench.simulator.protocols.fab2.messages;

import byzzbench.simulator.protocols.fab2.Pair;
import byzzbench.simulator.protocols.pbft_java.message.IPhaseMessage;
import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.transport.messages.MessageWithRound;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

/**
 * <p>Message sent by Acceptor replicas to Learner replicas to inform them that a value has been accepted.</p>
 */
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@With
public class AcceptMessage extends IPhaseMessage implements MessageWithRound {
    private final String replicaId;
    private final Pair valueAndProposalNumber;

    public String getType() {
        return "ACCEPT";
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
        return 5 * (getSequenceNumber() - 1) + 2;
    }
}
