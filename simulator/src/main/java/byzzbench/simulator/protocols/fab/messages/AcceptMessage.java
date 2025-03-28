package byzzbench.simulator.protocols.fab.messages;

import byzzbench.simulator.protocols.fab.Pair;
import byzzbench.simulator.protocols.pbft_java.message.IPhaseMessage;
import byzzbench.simulator.transport.MessagePayload;
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
public class AcceptMessage extends IPhaseMessage {
    private final String replicaId;
    private final Pair valueAndProposalNumber;

    public String getType() {
        return "ACCEPT";
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
