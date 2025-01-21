package byzzbench.simulator.protocols.fab2.messages;

import byzzbench.simulator.protocols.fab.ProgressCertificate;
import byzzbench.simulator.protocols.fab2.ProposalNumber;
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
public class QueryMessage extends MessagePayload {
    private final ProposalNumber proposalNumber;

    public String getType() {
        return "QUERY";
    }

//    @Override
//    public long getViewNumber() {
//        return proposalNumber.getViewNumber();
//    }
//
//    @Override
//    public long getSequenceNumber() {
//        return 0;
//    }

//    @Override
//    public long getRound() {
//        return 0;
//    }

//    @Override
//    public byte[] getDigest() {
//        return new byte[0];
//    }
}
