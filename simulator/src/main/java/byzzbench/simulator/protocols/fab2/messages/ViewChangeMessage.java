package byzzbench.simulator.protocols.fab2.messages;

import byzzbench.simulator.protocols.fab2.ProposalNumber;
import byzzbench.simulator.protocols.pbft_java.message.IPhaseMessage;
import byzzbench.simulator.transport.MessagePayload;
import lombok.*;

/**
 * <p>Message sent by a replica to indicate the election of a new leader.</p>
 */
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@With
public class ViewChangeMessage extends MessagePayload {
    private final String senderId;
    private final ProposalNumber proposalNumber;
    private final String newLeaderId;

    public String getType() {
        return "VIEW_CHANGE";
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
//
//    @Override
//    public long getRound() {
//        return 0;
//    }
//
//    @Override
//    public byte[] getDigest() {
//        return newLeaderId.getBytes();
//    }
}
