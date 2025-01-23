package byzzbench.simulator.protocols.fab2.messages;

import byzzbench.simulator.protocols.fab2.Pair;
import byzzbench.simulator.protocols.pbft_java.message.IPhaseMessage;
import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.transport.messages.MessageWithRound;
import lombok.*;

/**
 * <p>Message sent by Proposer replicas to (the other) Proposer replicas to inform them that a value has been accepted.</p>
 */
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@With
public class SatisfiedMessage extends IPhaseMessage implements MessageWithRound {
    private final String senderId;
    private final Pair valueAndProposalNumber;
    public String getType() {
        return "SATISFIED";
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
        return 5 * getSequenceNumber() + 1;
    }
}
