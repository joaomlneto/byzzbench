package byzzbench.simulator.protocols.fab.messages;

import byzzbench.simulator.protocols.fab.ProgressCertificate;
import byzzbench.simulator.protocols.fab.Pair;
import byzzbench.simulator.protocols.pbft_java.message.IPhaseMessage;
import byzzbench.simulator.transport.MessagePayload;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

/**
 * <p>Message sent by Proposer replicas to Acceptor replicas to propose a value.</p>
 */
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@With
public class ProposeMessage extends IPhaseMessage {
    private final String replicaId;
    private final Pair valueAndProposalNumber;
    private final ProgressCertificate progressCertificate;

    @Override
    public String getType() {
        return "PROPOSE";
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
