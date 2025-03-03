package byzzbench.simulator.protocols.fab.messages;

import byzzbench.simulator.protocols.fab.Pair;
import byzzbench.simulator.protocols.pbft_java.message.IPhaseMessage;
import byzzbench.simulator.transport.MessagePayload;
import lombok.*;

/**
 * <p>Message sent by Proposer replicas to (the other) Proposer replicas to inform them that a value has been accepted.</p>
 */
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@With
public class SatisfiedMessage extends IPhaseMessage {
    private final String senderId;
    private final Pair valueAndProposalNumber;
    public String getType() {
        return "SATISFIED";
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
