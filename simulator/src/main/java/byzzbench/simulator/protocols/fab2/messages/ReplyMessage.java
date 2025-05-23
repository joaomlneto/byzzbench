package byzzbench.simulator.protocols.fab2.messages;

import byzzbench.simulator.protocols.fab2.Pair;
import byzzbench.simulator.protocols.pbft_java.message.IPhaseMessage;
import byzzbench.simulator.transport.MessagePayload;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

/**
 * <p>Message sent by Acceptor replicas to new Leader replica to recover after new leader election.</p>
 */
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@With
public class ReplyMessage extends IPhaseMessage {
    private final Pair valueAndProposalNumber;
    private final boolean isSigned;
    private final String sender;
    private final String clientID;

    public String getType() {
        return "RESPONSE";
    }

    @Override
    public long getViewNumber() {
        return valueAndProposalNumber.getProposalNumber().getViewNumber();
    }

    @Override
    public long getSequenceNumber() {
//        return valueAndProposalNumber.getProposalNumber().getSequenceNumber();
        return 0;
    }

    @Override
    public long getRound() {
        return 0;
    }

    @Override
    public byte[] getDigest() {
        return valueAndProposalNumber.getValue();
    }
}
