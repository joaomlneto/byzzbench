package byzzbench.simulator.protocols.fab.messages;

import byzzbench.simulator.protocols.fab.ProgressCertificate;
import byzzbench.simulator.protocols.pbft_java.message.IPhaseMessage;
import byzzbench.simulator.transport.MessagePayload;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

/**
 * <p>Message sent by Proposer replicas to Acceptor replicas to recover after new leader election.</p>
 */
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@With
public class QueryMessage extends IPhaseMessage {
    private final long proposalNumber;

    public String getType() {
        return "QUERY";
    }

    @Override
    public long getViewNumber() {
        return proposalNumber;
    }

    @Override
    public long getSequenceNumber() {
        return proposalNumber;
    }

    @Override
    public byte[] getDigest() {
        return new byte[0];
    }
}
