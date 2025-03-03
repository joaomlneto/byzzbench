package byzzbench.simulator.protocols.fab2.messages;

import byzzbench.simulator.protocols.fab2.ProposalNumber;
import byzzbench.simulator.protocols.pbft_java.message.IPhaseMessage;
import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.transport.messages.MessageWithRound;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

/**
 * <p>Message sent by Learner replicas to Learner replicas to request the other's learned value.</p>
 */
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@With
public class PullMessage extends IPhaseMessage implements MessageWithRound {
    private final ProposalNumber proposalNumber;

    public String getType() {
        return "PULL";
    }

    @Override
    public long getViewNumber() {
        return proposalNumber.getViewNumber();
    }

    @Override
    public long getSequenceNumber() {
        return proposalNumber.getSequenceNumber();
    }

    @Override
    public byte[] getDigest() {
        return new byte[0];
    }

    @Override
    public long getRound() {
        return 5 * getSequenceNumber() + 4;
    }
}
